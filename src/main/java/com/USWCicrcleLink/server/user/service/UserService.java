package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailService;
import com.USWCicrcleLink.server.email.service.EmailTokenService;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.dto.TokenDto;
import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.AuthToken;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.*;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import com.USWCicrcleLink.server.user.repository.UserTempRepository;
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
    private final JwtProvider jwtProvider;


    public boolean confirmPW(UUID uuid, String userpw){
        User user = mypageService.getUserByUUID(uuid);
        return user.getUserPw().equals(userpw);
    }

    public void updateNewPW(UUID uuid, String userPw, String newPW, String confirmNewPW){

        if (newPW.trim().isEmpty() || confirmNewPW.trim().isEmpty()) {
            throw new UserException(ExceptionType.USER_PASSWORD_NOT_INPUT);
        }

        if (!newPW.equals(confirmNewPW)) {
            throw new UserException(ExceptionType.USER_NEW_PASSWORD_NOT_MATCH);
        }

        if (!confirmPW(uuid, userPw)) {
            throw new UserException(ExceptionType.USER_PASSWORD_NOT_MATCH);
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

        log.info("임시 회원 생성 메서드 실행");
        // 회원 테이블 이메일 중복 검증
        verifyUserDuplicate(request.getEmail());

        log.info("임시 회원 생성 완료 email= {}", request.getEmail());
        return userTempRepository.save(request.toEntity());

    }

    private void verifyUserDuplicate(String email){
        log.info("이메일 중복 검증 시작 email= {}",email);
        userRepository.findByEmail(email)
                .ifPresent(user-> {
                    throw new UserException(ExceptionType.USER_OVERLAP);
                });
        log.info("이메일 중복 검증 완료");
    }

    public UserTemp verifyEmailToken(UUID emailToken_uuid) {

        log.info("이메일 토큰 검증 시작");
        // 토큰 검증
        emailTokenService.verifyEmailToken(emailToken_uuid);

        // 검증된 임시 회원 가져오기
        EmailToken token = emailTokenService.getEmailToken(emailToken_uuid);
        log.info("이메일 토큰 검증 완료: {}", emailToken_uuid);
        return token.getUserTemp();
    }

    // 회원가입
    @Transactional
    public void signUp(UserTemp userTemp) {

        log.info("회원 가입 요청 시작");
        User user = User.createUser(userTemp);
        Profile profile = Profile.createProfile(userTemp, user);

        // 회원 가입
        userRepository.save(user);
        profileRepository.save(profile);
        log.info("회원 가입 완료 account = {}", user.getUserAccount());

        // 임시 회원 정보 삭제
        emailTokenService.deleteEmailTokenAndUserTemp(userTemp);
        log.info("임시 회원 정보 삭제 완료");
    }

    public void verifyAccountDuplicate(String account) {
        log.info("계정 중복 체크 요청 시작 account = {}",account);
            userRepository.findByUserAccount(account)
                    .ifPresent(user-> {
                        throw new UserException(ExceptionType.USER_ACCOUNT_OVERLAP);
                    });
        log.info("계정 중복 확인 완료");
    }

    // 로그인
    public TokenDto logIn(LogInRequest request) {
        log.info("로그인 요청: {}", request.getAccount());

        Optional<User> user = userRepository.findByUserAccount(request.getAccount());

        if (user.isEmpty() || !user.get().getUserPw().equals(request.getPassword())) {
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }

        log.info("JWT 생성");
        String accessToken = jwtProvider.createAccessToken(user.get().getUserUUID().toString());

        log.info("로그인 성공, 엑세스 토큰: {}", accessToken);
        return new TokenDto(accessToken);
    }


    public void validatePasswordsMatch(PasswordRequest request) {
        log.info("비밀번호 일치 확인 요청 시작");
        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new UserException(ExceptionType.USER_PASSWORD_MISMATCH);
        }
        log.info("비밀번호 일치 확인 완료");
    }

    public User findUser(String email) {
        log.info("계정 찾기 요청  email= {}",email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
    }

    public User validateAccountAndEmail(UserInfoDto request) {
        log.info("아이디와 이메일 유효성 검증 시작");
        return userRepository.findByUserAccountAndEmail(request.getUserAccount(), request.getEmail())
                .orElseThrow(() -> new UserException(ExceptionType.USER_INVALID_ACCOUNT_AND_EMAIL));

    }

    // 비밀번호 재설정
    public void resetPW(User user, PasswordRequest request) {

        // 비밀번호 일치 확인
        validatePasswordsMatch(request);
        log.info("새로운 비밀번호 일치 확인 완료");

        // 새로운 비밀번호로 업데이트
        user.updateUserPw(request.getPassword());
        userRepository.save(user);

        log.info("새로운 비밀번호 변경 완료 userUUID = {}", user.getUserUUID());
    }

    public User findByUuid(UUID uuid) {
        return userRepository.findByUserUUID(uuid).orElseThrow(() -> new UserException(ExceptionType.USER_UUID_NOT_FOUND));
    }

    // 회원 가입 메일 생성 및 전송
    @Transactional
    public void sendSignUpMail(UserTemp userTemp,EmailToken emailToken)  {
        log.info("회원 가입 인증 메일 요청 ");
        MimeMessage message = emailService.createSingUpLink(userTemp,emailToken);
        emailService.sendEmail(message);
        log.info("회원가입 인증메일 전송 완료 emailToken_uuid= {} ",emailToken.getEmailTokenUUID());
    }

    @Transactional
    public void sendAuthCodeMail(User user, AuthToken authToken)  {
        log.info("비밀번호 찾기  메일 생성 요청");
        MimeMessage message = emailService.createAuthCodeMail(user,authToken);
        emailService.sendEmail(message);
        log.info("비밀번호 찾기 메일 전송 완료");
    }

    @Transactional
    public void sendAccountInfoMail (User findUser)  {
        log.info("아이디 찾기 메일 생성 요청");
        MimeMessage message = emailService.createAccountInfoMail(findUser);
        emailService.sendEmail(message);
        log.info("아이디 찾기 메일 전송 완료 email=  {} ",findUser.getEmail());
    }

    // 회원 가입 확인
    @Transactional(readOnly = true)
    public String signUpFinish(String account) {

        log.info("회원 가입 완료 처리 요청 ");
        // 계정이 존재하는지 확인
        userRepository.findByUserAccount(account)
                .orElseThrow(() -> new UserException(ExceptionType.USER_ACCOUNT_NOT_EXISTS));

        log.info("최종 회원 가입 완료");
        return "true";
    }
}