package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailService;
import com.USWCicrcleLink.server.email.service.EmailTokenService;
import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.*;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.dto.TokenDto;
import com.USWCicrcleLink.server.global.security.service.CustomUserDetailsService;
import com.USWCicrcleLink.server.global.security.util.CustomUserDetails;
import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import com.USWCicrcleLink.server.profile.domain.MemberType;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.profile.service.ProfileService;
import com.USWCicrcleLink.server.user.domain.*;
import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberTemp;
import com.USWCicrcleLink.server.user.dto.*;
import com.USWCicrcleLink.server.user.repository.ClubMemberTempRepository;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import com.USWCicrcleLink.server.user.repository.UserTempRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserTempRepository userTempRepository;
    private final EmailService emailService;
    private final EmailTokenService emailTokenService;
    private final ProfileRepository profileRepository;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ProfileService profileService;
    private final ClubMemberTempRepository clubMemberTempRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberAccountStatusService clubMemberAccountStatusService;

    private static final int FCM_TOKEN_CERTIFICATION_TIME = 60;

    // 비밀번호 조건
    private static final Pattern letterPattern = Pattern.compile("[a-zA-Z]");
    private static final Pattern numberPattern = Pattern.compile("[0-9]");
    private static final Pattern specialCharPattern = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`]");


    // 어세스토큰에서 유저정보 가져오기
    public User getUserByAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.user();
    }

    //현재 비밀번호 확인
    private boolean confirmPW(String userpw) {
        User user = getUserByAuth();
        return passwordEncoder.matches(userpw, user.getUserPw());
    }

    //비밀번호 변경
    public void updateNewPW(UpdatePwRequest updatePwRequest) {

        if (!confirmPW(updatePwRequest.getUserPw())) {
            throw new UserException(ExceptionType.USER_PASSWORD_NOT_MATCH);
        }
        // 비밀번호 칸이 빈칸인지 확인
        checkPasswordFieldBlank(updatePwRequest.getNewPw(), updatePwRequest.getNewPw());
        // 새로운 비밀번호의 유효성 검사
        checkPasswordCondition(updatePwRequest.getNewPw());
        // 비밀번호가 일치하는지 확인
        checkPasswordMatch(updatePwRequest.getNewPw(), updatePwRequest.getConfirmNewPw());

        User user = getUserByAuth();
        String encryptedNewPw = passwordEncoder.encode(updatePwRequest.getNewPw());
        user.updateUserPw(encryptedNewPw);

        try {
            userRepository.save(user);  // 비밀번호 업데이트
        } catch (Exception e) {
            log.error("비밀번호 업데이트 실패 {}", user.getUserId());
            throw new UserException(ExceptionType.PROFILE_UPDATE_FAIL);
        }
        log.info("비밀번호 변경 완료: {}", user.getUserId());
    }

    // 비밀번호 칸이 빈칸인지 확인
    private void checkPasswordFieldBlank(String password, String comfimPw) {
        if (password.trim().isEmpty() || comfimPw.trim().isEmpty()) {
            throw new UserException(ExceptionType.USER_PASSWORD_NOT_INPUT);
        }
    }

    // 비밀번호 일치 확인
    private void checkPasswordMatch(String password, String confirmPw) {
        if (!password.equals(confirmPw)) {
            throw new UserException(ExceptionType.USER_NEW_PASSWORD_NOT_MATCH);
        }
    }

    // 비밀번호 조건이 충족되는지 확인
    private void checkPasswordCondition(String password) {
        if (!letterPattern.matcher(password).find() || !numberPattern.matcher(password).find() || !specialCharPattern.matcher(password).find()) {
            throw new UserException(ExceptionType.USER_PASSWORD_CONDITION_FAILED);
        }
    }

    // 전화번호 - 제거하는 메서드
    public String removeHyphensFromPhoneNumber(String telephone) {
        if (telephone.contains("-")) {
            return telephone.replaceAll("-", "");
        }
        return telephone;
    }


    // 임시 회원 생성 및 저장
    public UserTemp registerUserTemp(SignUpRequest request) {

        log.debug("임시 회원 생성 메서드 실행");

        // 임시 회원 정보 존재시 기존 데이터 삭제
        userTempRepository.findByTempEmail(request.getEmail())
                .ifPresent(userTemp -> {
                    emailTokenService.deleteEmailToken(userTemp);
                    log.debug("중복된 임시 회원 데이터 삭제: userTemp_email= {}", request.getEmail());
                });

        // 회원 테이블 이메일 중복 검증
        verifyUserDuplicate(request.getEmail());

        // 번호만 추출해서 저장하기
        String telephone = removeHyphensFromPhoneNumber(request.getTelephone());

        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("임시 회원 생성 완료 email= {}", request.getEmail());

        return userTempRepository.save(request.toEntity(encodedPassword, telephone));
    }

    // 기존 회원가입 - 임시 동아리원 생성하기
    public ClubMemberTemp registerClubMemberTemp(ExistingMemberSignUpRequest request) {
        log.debug("임시 동아리원 등록 시작 - 이름: {}, 전화번호: {}, 지원한 동아리 개수: {}",
                request.getUserName(), request.getTelephone(), request.getClubs().size());

        // 지원한 동아리의 개수
        int total = request.getClubs().size();

        // 전화번호 - 제거
        String telephone = removeHyphensFromPhoneNumber(request.getTelephone());
        log.debug("전화번호 형식 변환 완료 - 원본: {}, 변환 후: {}", request.getTelephone(), telephone);

        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("비밀번호 인코딩 완료 - 사용자 이름: {}", request.getUserName());

        // 엔터티 저장
        ClubMemberTemp savedEntity;
        try{
            savedEntity = clubMemberTempRepository.save(request.toEntity(encodedPassword, telephone, total));
            log.debug("임시 동아리원 등록 완료 - 저장된 엔터티: {}", savedEntity);
        }catch (Exception e){
            log.error("임시 동아리원 등록 실패 - 사용자 이름: {}", request.getUserName());
            throw new ClubMemberTempException(ExceptionType.CLUB_MEMBERTEMP_CREATE_FAILED);
        }

        return savedEntity;
    }


    // 동아리 회장에게 가입신청 보내기
    public void sendRequest(ExistingMemberSignUpRequest request, ClubMemberTemp clubMemberTemp) {
        log.debug("가입신청 시작 - 사용자: {}, 요청 동아리 개수: {}",
                clubMemberTemp.getProfileTempName(), clubMemberTemp.getTotalClubRequest());

        // 동아리 정보 조회
        for (ClubDTO clubDto : request.getClubs()) {
            log.debug("동아리 정보 조회 중 - Club ID: {}", clubDto.getClubId());
            Club club = clubRepository.findById(clubDto.getClubId())
                    .orElseThrow(() -> {
                        log.error("동아리 조회 실패 - 존재하지 않는 동아리 ID: {}", clubDto.getClubId());
                        return new ClubException(ExceptionType.CLUB_NOT_EXISTS);
                    });
            log.debug("동아리 조회 성공 - Club ID: {}, 동아리 이름: {}", club.getClubId(), club.getClubName());

            // accountStatus 객체 생성하기
            clubMemberAccountStatusService.createAccountStatus(club,clubMemberTemp);
        }
        // 요청이 전부 제대로 갔는지 검증
        clubMemberAccountStatusService.checkRequest(request, clubMemberTemp);
    }



    // 이메일 중복 검증
    private void verifyUserDuplicate(String email){
        log.debug("이메일 중복 검증 시작 email= {}",email);
        userRepository.findByEmail(email)
                .ifPresent(user-> {
                    throw new UserException(ExceptionType.USER_OVERLAP);
                });
        log.debug("이메일 중복 검증 완료");
    }

    public UserTemp verifyEmailToken(UUID emailToken_uuid) {

        log.debug("이메일 토큰 검증 시작");
        // 토큰 검증
        emailTokenService.verifyEmailToken(emailToken_uuid);

        // 검증된 임시 회원 가져오기
        EmailToken token = emailTokenService.getEmailToken(emailToken_uuid);
        log.debug("이메일 토큰 검증 완료: {}", emailToken_uuid);
        return token.getUserTemp();
    }

    // userTemp -> user,profile 객체 생성
    public void createUserAndProfile(UserTemp userTemp){
        User user = User.createUser(userTemp);
        Profile profile = Profile.createProfile(userTemp, user);

        userRepository.save(user);
        profileRepository.save(profile);
    }

    // 회원가입
    public void signUp(UserTemp userTemp) {

        log.debug("회원 가입 요청 시작");
        createUserAndProfile(userTemp);
        log.debug("회원 가입 완료 account = {}", userTemp.getTempAccount());

        // 임시 회원 정보와 관련된 이메일 토큰 삭제
        emailTokenService.deleteEmailToken(userTemp);
        log.debug("임시 회원 정보 삭제 완료");
    }


    // 아이디 중복 검증
    public void verifyAccountDuplicate(String account) {
        log.debug("계정 중복 체크 요청 시작 account = {}",account);
            userRepository.findByUserAccount(account)
                    .ifPresent(user-> {
                        throw new UserException(ExceptionType.USER_ACCOUNT_OVERLAP);
                    });
        log.debug("계정 중복 확인 완료");
    }

    // 로그인
    public TokenDto logIn(LogInRequest request, HttpServletResponse response) {

        // 사용자 정보 조회 (UserDetails 사용)
        UserDetails userDetails = customUserDetailsService.loadUserByAccountAndRole(request.getAccount(), Role.USER);

        // UserDetails에서 User 객체 추출
        User user;
        if (userDetails instanceof CustomUserDetails) {
            user = ((CustomUserDetails) userDetails).user();
        } else {
            throw new UserException(ExceptionType.USER_NOT_EXISTS);
        }

        // 아이디와 비밀번호 검증
        if (!user.getUserAccount().equals(request.getAccount()) || !passwordEncoder.matches(request.getPassword(), user.getUserPw()) ) {
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }

        // 로그인 성공 시 토큰 발급
        String accessToken = jwtProvider.createAccessToken(userDetails.getUsername(), response);
        String refreshToken = jwtProvider.createRefreshToken(userDetails.getUsername(), response);

        log.debug("로그인 성공, uuid: {}", userDetails.getUsername());

        // fcm 토큰 저장
        Profile profile = profileRepository.findById(user.getUserId())
                    .orElseThrow(() -> new UserException(ExceptionType.USER_PROFILE_NOT_FOUND));

        profile.updateFcmTokenTime(request.getFcmToken(), LocalDateTime.now().plusDays(FCM_TOKEN_CERTIFICATION_TIME));
        profileRepository.save(profile);
        log.debug("fcmToken 업데이트 완료: {}", user.getUserAccount());

        return new TokenDto(accessToken, refreshToken);
    }

   // 비밀번호 유효성 검사
   @Transactional(readOnly = true)
    public void validatePassword(PasswordRequest request) {

       log.debug("비밀번호 유효성 확인 요청 시작");

       // 비밀번호 칸이 공백인지 확인
       checkPasswordFieldBlank(request.getPassword(), request.getConfirmPassword());
       // 비밀번호 조건이 충족되는지 확인
       checkPasswordCondition(request.getPassword());
       // 두 비밀번호 일치 확인
       checkPasswordMatch(request.getPassword(),request.getConfirmPassword());

       log.debug("비밀번호 유효성 검증 완료");
    }

    @Transactional(readOnly = true)
    public User findUser(String email) {
        log.debug("계정 찾기 요청  email= {}",email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
    }

    @Transactional(readOnly = true)
    public User validateAccountAndEmail(UserInfoDto request) {
        log.debug("아이디와 이메일 유효성 검증 시작");
        return userRepository.findByUserAccountAndEmail(request.getUserAccount(), request.getEmail())
                .orElseThrow(() -> new UserException(ExceptionType.USER_INVALID_ACCOUNT_AND_EMAIL));
    }

    // 비밀번호 재설정
    public void resetPW(UUID uuid, PasswordRequest request) {

        // 회원 조회
        User user = userRepository.findByUserUUID(uuid).orElseThrow(() -> new UserException(ExceptionType.USER_UUID_NOT_FOUND));

        // 새로운 비밀번호의 유효성 검사
        validatePassword(request);

        log.debug("비밀번호 유효성 검증 완료");

        user.updateUserPw(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        log.debug("새로운 비밀번호 변경 완료 userUUID = {}", user.getUserUUID());
    }

    // 회원 가입 메일 생성 및 전송
    @RateLimite(action = "EMAIL_VERIFICATION")
    public void sendSignUpMail(UserTemp userTemp,EmailToken emailToken)  {
        log.debug("회원 가입 인증 메일 요청 ");
        MimeMessage message = emailService.createSingUpLink(userTemp,emailToken);
        emailService.sendEmail(message);
        log.debug("회원가입 인증메일 전송 완료 emailToken_uuid= {} ",emailToken.getEmailTokenUUID());
    }

    // 비밀번호 변경을 위한 인증 코드 메일 전송
    public void sendAuthCodeMail(User user, AuthToken authToken)  {
        log.debug("비밀번호 찾기  메일 생성 요청");
        MimeMessage message = emailService.createAuthCodeMail(user,authToken);
        emailService.sendEmail(message);
        log.debug("비밀번호 찾기 메일 전송 완료");
    }

    // 아이디 찾기 메일 전송
    @RateLimite(action = "ID_FOUND_EMAIL")
    public void sendAccountInfoMail (User findUser)  {
        log.debug("아이디 찾기 메일 생성 요청");
        MimeMessage message = emailService.createAccountInfoMail(findUser);
        emailService.sendEmail(message);
        log.debug("아이디 찾기 메일 전송 완료 email=  {} ",findUser.getEmail());
    }

    // 회원 탈퇴 메일 전송
    public void sendWithdrawalCodeMail (WithdrawalToken token)  {
        log.debug("회원 탈퇴 메일 생성 요청");
        User findUser = getUserByAuth();
        MimeMessage message = emailService.createWithdrawalCodeMail(findUser,token);
        emailService.sendEmail(message);
        log.debug("회원 탈퇴 메일 전송 완료 email=  {} ",findUser.getEmail());
    }

    // 회원 가입 확인
    @Transactional(readOnly = true)
    public String signUpFinish(String account) {

        log.debug("회원 가입 완료 처리 요청 ");
        // 계정이 존재하는지 확인
        userRepository.findByUserAccount(account)
                .orElseThrow(() -> new UserException(ExceptionType.USER_ACCOUNT_NOT_EXISTS));

        log.debug("최종 회원 가입 완료");
        return "true";
    }

    // 회원 탈퇴
    public void cancelMembership(HttpServletRequest request, HttpServletResponse response) {

        // 리프레시 토큰 추출
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        if (refreshToken != null && jwtProvider.validateRefreshToken(refreshToken)) {
            // 유효한 리프레시 토큰인 경우, 리프레시 토큰 삭제
            String uuid = jwtProvider.getUUIDFromRefreshToken(refreshToken);
            jwtProvider.deleteRefreshTokenCookie(response);
            jwtProvider.deleteRefreshTokensByUuid(uuid);
            log.debug("리프레시 토큰 삭제 : 사용자 {}의 모든 리프레시 토큰 삭제 완료", uuid);
        } else {
            log.debug("리프레시 토큰이 존재하지 않거나 유효하지 않음. 회원 탈퇴 계속 진행.");
        }

        // 회원과 관련된 정보 모두 삭제
        profileService.deleteAll();
        userRepository.delete(getUserByAuth());

        log.debug("회원 탈퇴 성공");
    }

    // 로그인 가능 여부 판단
    public void verifyLogin(LogInRequest request) {
        log.debug("로그인 검증 시작 - 요청 계정: {}", request.getAccount());

        // account로 user 조회
        User user = userRepository.findByUserAccount(request.getAccount())
                .orElseThrow(() -> {
                    log.error("로그인 실패 - 존재하지 않는 계정: {}", request.getAccount());
                    return new UserException(ExceptionType.USER_ACCOUNT_NOT_EXISTS);
                });
        log.debug("사용자 조회 성공 - 계정: {}, 사용자 ID: {}", user.getUserAccount(), user.getUserId());

        // user로 프로필 조회하여 로그인 가능 여부 판단
        Profile profile = profileRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> {
                    log.error("로그인 실패 - 프로필 없음 - 사용자 ID: {}", user.getUserId());
                    return new ProfileException(ExceptionType.PROFILE_NOT_EXISTS);
                });
        log.debug("프로필 조회 성공 - 사용자 ID: {}, 회원 타입: {}", user.getUserId(), profile.getMemberType());

        if (profile.getMemberType().equals(MemberType.NONMEMBER)) { // 비회원인 경우 로그인 불가
            log.error("로그인 실패 - 비회원 사용자 - 사용자 ID: {}", user.getUserId());
            throw new UserException(ExceptionType.USER_LOGIN_FAILED);
        }

        log.debug("로그인 검증 완료 - 로그인 가능 사용자 ID: {}", user.getUserId());
    }

}