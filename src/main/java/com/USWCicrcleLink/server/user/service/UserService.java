package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailService;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.AuthToken;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.*;
import com.USWCicrcleLink.server.user.repository.AuthTokenRepository;
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
    private final AuthTokenRepository authTokenRepository;
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

    // 임시 회원 저장
    public UserTemp registerUserTemp(SignUpRequest request) {

        // 임시 회원 테이블 이메일 중복 검증
        if (userTempRepository.existsByTempEmail(request.getEmail())) {
            Optional<UserTemp> userTemp = userTempRepository.findByTempEmail(request.getEmail());
            // 해당 임시 회원 정보 삭제
            emailService.deleteUserTempAndEmailToken(userTemp.get());
        }
        // 회원 테이블 이메일 중복 검증
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다");
        }

        return userTempRepository.save(request.toEntity());
    }


    @Transactional
    public void sendEmail(UserTemp userTemp) throws MessagingException {
        emailService.sendEmail(emailService.createTokenAndEmail(userTemp));
    }

    public UserTemp verifyToken (UUID emailTokenId) {

        // 토큰 검증
        emailService.validateToken(emailTokenId);
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

    public User findUser(String email) {

        if(!userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("존재하지 않는 이메일 입니다.");
        }
        return userRepository.findByEmail(email);
    }

    public void sendEmailInfo(User findUser) throws MessagingException {
        emailService.sendEmailInfo(findUser);
    }

    public User validateAccountAndEmail(FindUserInfoRequest request) {

        Optional<User> user = userRepository.findByEmailAndUserAccount(request.getEmail(),request.getUserAccount());
        if(user.isEmpty()){
            throw new IllegalArgumentException("올바르지 않은 아이디 혹은 이메일 입니다");
        }
        return user.get();
    }
    public void sendAuthCode(User user, FindUserInfoRequest request) throws MessagingException {
        emailService.sendAuthCode(user,request.getEmail());
    }


    public void resetPW(User user, UpdatePwRequest request) {

        if(!request.getNewPw().equals(request.getConfirmNewPw())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        user.updateUserPw(request.getNewPw());
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

    public void validateAuthToken(UUID uuid, FindUserInfoRequest request) {
        AuthToken authToken = authTokenRepository.findByUserUserUUID(uuid)
                .orElseThrow(() -> new IllegalArgumentException("인증 코드를 찾을 수 없습니다"));

        if(!authToken.isAuthCodeValid(request.getAuthCode())){
            throw new IllegalArgumentException("인증코드가 일치 하지않습니다");
        }
    }
}