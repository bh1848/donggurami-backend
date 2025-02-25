package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import com.USWCicrcleLink.server.global.security.jwt.dto.TokenDto;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.security.details.service.CustomUserDetailsService;
import com.USWCicrcleLink.server.profile.domain.MemberType;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.LogInRequest;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthService {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int FCM_TOKEN_CERTIFICATION_TIME = 60;

    /**
     * User 로그인
     */
    public TokenDto userLogin(LogInRequest request, HttpServletResponse response) {

        UserDetails userDetails = customUserDetailsService.loadUserByAccountAndRole(request.getAccount(), Role.USER);

        UUID userUUID = extractUserUUID(userDetails);

        User user = userRepository.findByUserUUID(userUUID)
                .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));

        if (!user.getUserAccount().equals(request.getAccount()) || !passwordEncoder.matches(request.getPassword(), user.getUserPw())) {
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }

        String accessToken = jwtProvider.createAccessToken(userUUID, response);
        String refreshToken = jwtProvider.createRefreshToken(userUUID, response, true);

        log.debug("로그인 성공, uuid: {}", userUUID);

        // FCM 토큰 저장
        profileRepository.findByUser_UserUUID(userUUID).ifPresent(profile -> {
            profile.updateFcmTokenTime(request.getFcmToken(), LocalDateTime.now().plusDays(FCM_TOKEN_CERTIFICATION_TIME));
            profileRepository.save(profile);
            log.debug("FCM 토큰 업데이트 완료: {}", user.getUserAccount());
        });

        return new TokenDto(accessToken, refreshToken);
    }

    // UserDetails에서 UUID 추출
    private UUID extractUserUUID(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.user().getUserUUID();
        }
        throw new UserException(ExceptionType.USER_NOT_EXISTS);
    }

    // 로그인 가능 여부 판단
    public void verifyLogin(LogInRequest request) {
        log.debug("로그인 검증 시작 - 요청 계정: {}", request.getAccount());

        // account로 user 조회
        User user = userRepository.findByUserAccount(request.getAccount())
                .orElseThrow(() -> {
                    log.debug("로그인 실패 - 존재하지 않는 계정: {}", request.getAccount());
                    return new UserException(ExceptionType.USER_ACCOUNT_NOT_EXISTS);
                });
        log.debug("사용자 조회 성공 - 계정: {}, 사용자 ID: {}", user.getUserAccount(), user.getUserId());

        // user로 프로필 조회하여 로그인 가능 여부 판단
        Profile profile = profileRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> {
                    log.debug("로그인 실패 - 프로필 없음 - 사용자 ID: {}", user.getUserId());
                    return new ProfileException(ExceptionType.PROFILE_NOT_EXISTS);
                });
        log.debug("프로필 조회 성공 - 사용자 ID: {}, 회원 타입: {}", user.getUserId(), profile.getMemberType());

        // 비회원인 경우 로그인 불가
        if (profile.getMemberType().equals(MemberType.NONMEMBER)) {
            log.debug("로그인 실패 - 비회원 사용자 - 사용자 ID: {}", user.getUserId());
            throw new UserException(ExceptionType.USER_LOGIN_FAILED);
        }

        log.debug("로그인 검증 완료 - 로그인 가능 사용자 ID: {}", user.getUserId());
    }

    /**
     * User 로그아웃
     */
    public void userLogout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        if (refreshToken != null && jwtProvider.validateRefreshToken(refreshToken, true)) {
            jwtProvider.getUUIDFromRefreshToken(refreshToken, true);
        } else {
            log.debug("USER 로그아웃 - 리프레시 토큰 없음 또는 유효하지 않음");
        }

        // 클라이언트 쿠키에서 리프레시 토큰 삭제
        jwtProvider.deleteRefreshTokenCookie(response);
    }

    /**
     * User 토큰 재발급 (JWT 기반)
     */
    public TokenDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        if (refreshToken == null) {
            log.debug("리프레시 토큰 없음 - 로그아웃 처리 진행");
            forceLogout(response);
            return null;
        }

        if (!jwtProvider.validateRefreshToken(refreshToken, true)) {
            log.debug("유효하지 않은 리프레시 토큰 감지 - 로그아웃 처리 진행");
            forceLogout(response);
            return null;
        }

        UUID uuid = UUID.fromString(jwtProvider.getClaims(refreshToken).getSubject());
        log.debug("리프레시 토큰 검증 완료 - UUID: {}", uuid);

        String newAccessToken = jwtProvider.createAccessToken(uuid, response);
        String newRefreshToken = jwtProvider.createRefreshToken(uuid, response, true);

        log.debug("토큰 갱신 성공 - UUID: {}", uuid);
        return new TokenDto(newAccessToken, newRefreshToken);
    }

    // 로그아웃 처리 (쿠키 삭제)
    private void forceLogout(HttpServletResponse response) {
        jwtProvider.deleteRefreshTokenCookie(response);
        log.debug("리프레시 토큰 무효화 - 로그아웃 처리 완료");
    }
}
