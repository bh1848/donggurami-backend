package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailService;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.*;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import com.USWCicrcleLink.server.user.repository.UserTempRepository;

import jakarta.mail.MessagingException;

import jakarta.mail.internet.MimeMessage;
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
    private final MypageService mypageService;


    public boolean confirmPW(UUID uuid, String userpw){
        User user = mypageService.getUserByUUID(uuid);
        return user.getUserPw().equals(userpw);
    }

    public void updateNewPW(UUID uuid, String userPw, String newPW, String confirmNewPW){

        if (newPW.trim().isEmpty() || confirmNewPW.trim().isEmpty()) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인은 빈칸일 수 없습니다.");
        }

        if (!newPW.equals(confirmNewPW)) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if (!confirmPW(uuid, userPw)) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        User user = mypageService.getUserByUUID(uuid);
        user.updateUserPw(newPW);
        userRepository.save(user);

        log.info("비밀번호 변경 완료: {}",user.getUserUUID());
    }

    // 임시 회원 생성 및 저장
    public UserTemp registerUserTemp(SignUpRequest request) {

        // 중복 검증
        verificationDuplicate(request.getEmail());

        return userTempRepository.save(request.toEntity());
    }

    // 이메일 중복 검증
    private void verificationDuplicate(String email) {

        // 임시 회원 테이블 이메일 중복 검증
        Optional<UserTemp> findUserTemp = userTempRepository.findByTempEmail(email);
        findUserTemp.ifPresent(emailService::deleteUserTempAndEmailToken);

        // 회원 테이블 이메일 중복 검증
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다");
        }
    }


    @Transactional
    public void sendSignUpMail(UserTemp userTemp) throws MessagingException {
        MimeMessage message = emailService.createSingUpLink(userTemp);
        emailService.sendEmail(message);
    }

    public UserTemp verifyEmailToken(UUID emailTokenId) {

        // 토큰 검증
        emailService.verifyEmailToken(emailTokenId);
        // 검증된 임시 회원 가져오기
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
        emailService.deleteUserTempAndEmailToken(userTemp);

        return user;
    }

    public void validateAccountDuplicate(String account) {
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


    public void validatePasswordsMatch(PasswordRequest request) {
        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new IllegalStateException("비밀번호가 일치 하지 않습니다");
        }
    }

    public User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 입니다"));
    }

    public User verifyAccountAndEmail(UserInfoDto request) {
        return  userRepository.findByUserAccountAndEmail(request.getUserAccount(), request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("올바르지 않은 이메일 혹은 아이디 입니다"));
    }

    // 비밀번호 재설정
    public void resetPW(User user, PasswordRequest request) {

        // 비밀번호 일치 확인
        validatePasswordsMatch(request);

        // 새로운 비밀번호로 업데이트
        user.updateUserPw(request.getPassword());
        userRepository.save(user);

        log.info("새로운 비밀번호 변경 완료: {}", user.getUserUUID());
    }

    public User findByUuid(UUID uuid) {

        User user = userRepository.findByUserUUID(uuid);

        if (user == null) {
            throw new IllegalArgumentException("해당 UUID를 가진 사용자를 찾을 수 없습니다: " + uuid);
        }

        return  user;
    }


    @Transactional
    public void sendAuthCodeMail(User user) throws MessagingException {
        MimeMessage message = emailService.createAuthToken(user);
        emailService.sendEmail(message);
    }
}