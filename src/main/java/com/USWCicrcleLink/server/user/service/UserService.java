package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailService;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.CheckPasswordRequest;
import com.USWCicrcleLink.server.user.dto.LogInRequest;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import com.USWCicrcleLink.server.user.repository.UserTempRepository;

import jakarta.mail.MessagingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final UserTempRepository userTempRepository;
    private final EmailService emailService;
    private final ProfileRepository profileRepository;
    private final AuthCodeRepository authCodeRepository;

    public void updatePW(UUID uuid, String newPassword, String confirmNewPassword) {

        User user = userRepository.findByUserUUID(uuid);

        if (user == null) {
            throw new IllegalArgumentException("해당 UUID를 가진 사용자를 찾을 수 없습니다: " + uuid);
        }
        if (!confirmNewPassword.equals(user.getUserPw())) {
            throw new IllegalArgumentException("기존 비밀번호와 일치하지 않습니다.");
        }

        user.updateUserPw(newPassword);
        userRepository.save(user);

        log.info("비밀번호 변경 완료: {}",user.getUserUUID());
    }


    public UserTemp registerTempUser(SignUpRequest request) {

        // 임시 회원 테이블 이메일 중복 검증
        if (userTempRepository.existsByTempEmail(request.getEmail())) {
            Optional<UserTemp> userTemp = userTempRepository.findByTempEmail(request.getEmail());
            // 해당 임시 회원 정보 삭제
            emailService.deleteTempUserAndToken(userTemp.get());
        }
        // 회원 테이블 이메일 중복 검증
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다");
        }

        return userTempRepository.save(request.toEntity());
    }


    @Transactional
    public void sendEmail(UserTemp userTemp) throws MessagingException {
        emailService.createEmailToken(userTemp);
        emailService.sendEmail(emailService.createAuthLink(userTemp));
    }

    public UserTemp validateEmailToken(UUID emailTokenId) {

        // 토큰 검증
        emailService.validateToken(emailTokenId);
        EmailToken token = emailService.getTokenBy(emailTokenId);

        return token.getUserTemp();
    }

    // 회원가입
    @Transactional
    public User signUp(UserTemp userTemp) {

        User user = User.createUser(userTemp);
        Profile profile = Profile.createProfile(userTemp, user);

        // 회원 가입
        userRepository.save(user);
        profileRepository.save(profile);
        // 임시 회원 정보 삭제
        emailService.deleteTempUserAndToken(userTemp);

        return user;
    }

    public void checkAccountDuplicate(String account) {
        if (userRepository.existsByUserAccount(account)) {
            throw new IllegalStateException("중복된 ID 입니다. 새로운 ID를 입력해주세요");
        }
    }

    public String logIn(LogInRequest request)  {

        User user = userRepository.findByUserAccount(request.getAccount())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID입니다"));

        if (!user.getUserPw().equals(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        return user.getUserAccount();
    }

    // 비밀번호 일치 확인
    public void comparePasswords(CheckPasswordRequest request) {
        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new IllegalStateException("비밀번호가 일치 하지 않습니다");
        }
    }

    public Optional<User> findUser(String email) {

        if(!userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("존재하지 않는 이메일 입니다.");
        }
        return userRepository.findByEmail(email);
    }

    public void sendEmailInfo(User findUser) throws MessagingException {
        emailService.sendEmailInfo(findUser);
    }

    public void validateAccountAndEmail(String userAccount, String email) {

        Optional<User> user = userRepository.findByEmailAndUserAccount(email,userAccount);
        if(user.isEmpty()){
            throw new IllegalArgumentException("올바르지 않은 아이디 혹은 이메일 입니다");
        }

    }
    public void sendAuthCode(String email) throws MessagingException {
        emailService.sendAuthCode(email);
    }




}