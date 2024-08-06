package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailService;
import com.USWCicrcleLink.server.email.service.EmailTokenService;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.AuthToken;
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
    private final EmailTokenService emailTokenService;
    private final ProfileRepository profileRepository;
    private final MypageService mypageService;


    public boolean confirmPW(UUID uuid, String userpw){
        User user = mypageService.getUserByUUID(uuid);
        return user.getUserPw().equals(userpw);
    }

    public void updateNewPW(UUID uuid, String userPw, String newPW, String confirmNewPW){

        if (newPW.trim().isEmpty() || confirmNewPW.trim().isEmpty()) {
            throw new UserException(ExceptionType.PASSWORD_NOT_INPUT);
        }

        if (!newPW.equals(confirmNewPW)) {
            throw new UserException(ExceptionType.NEW_PASSWORD_NOT_MATCH);
        }

        if (!confirmPW(uuid, userPw)) {
            throw new UserException(ExceptionType.PASSWORD_NOT_MATCH);
        }

        User user = mypageService.getUserByUUID(uuid);
        user.updateUserPw(newPW);
        User updateUserPw = userRepository.save(user);

        if(updateUserPw == null){
            log.error("비밀번호 업데이트 실패");
            throw new UserException(ExceptionType.PROFILE_UPDATE_FAIL);
        }
        log.info("비밀번호 변경 완료: {}",user.getUserUUID());
    }

    // 임시 회원 생성 및 저장
    public UserTemp registerUserTemp(SignUpRequest request) {

        // 중복 검증
        verifyUserTempDuplicate(request.getEmail());
        verifyUserDuplicate(request.getEmail());

        return userTempRepository.save(request.toEntity());
    }

    // 임시 회원 테이블 이메일 중복 검증
    private void verifyUserTempDuplicate(String email) {
        // 임시 데이터 존재 시 삭제
        userTempRepository.findByTempEmail(email)
                .ifPresent(emailTokenService::deleteEmailTokenAndUserTemp);
    }

    // 회원 테이블 이메일 중복 검증
    private void verifyUserDuplicate(String email){

        userRepository.findByEmail(email)
                .ifPresent(user-> {
                    throw new IllegalStateException("이미 존재하는 회원 입니다");
                });
    }

    public UserTemp verifyEmailToken(UUID emailTokenId) {

        // 토큰 검증
        emailTokenService.verifyEmailToken(emailTokenId);
        // 검증된 임시 회원 가져오기
        EmailToken token = emailTokenService.getEmailToken(emailTokenId);

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
        emailTokenService.deleteEmailTokenAndUserTemp(userTemp);

        return user;
    }

    public void verifyAccountDuplicate(String account) {
            userRepository.findByUserAccount(account)
                    .ifPresent(user-> {
                        throw new IllegalStateException("이미 존재하는 계정 입니다");
                    });
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

    public User validateAccountAndEmail(UserInfoDto request) {
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
    public void sendSignUpMail(UserTemp userTemp,EmailToken emailToken) throws MessagingException {
        MimeMessage message = emailService.createSingUpLink(userTemp,emailToken);
        emailService.sendEmail(message);
    }

    @Transactional
    public void sendAuthCodeMail(User user, AuthToken authToken) throws MessagingException {
        MimeMessage message = emailService.createAuthCodeMail(user,authToken);
        emailService.sendEmail(message);
    }

    @Transactional
    public void sendAccountInfoMail (User findUser) throws MessagingException {
        MimeMessage message = emailService.createAccountInfoMail(findUser);
        emailService.sendEmail(message);
    }
}