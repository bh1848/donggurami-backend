package com.USWCicrcleLink.server.global.Integration.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.Integration.domain.LoginType;
import com.USWCicrcleLink.server.global.Integration.dto.IntegrationLoginRequest;
import com.USWCicrcleLink.server.global.Integration.dto.IntegrationLoginResponse;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.service.CustomUserDetailsService;
import com.USWCicrcleLink.server.global.security.util.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IntegrationService {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    // 동아리 회장, 동연회-개발자 통합 로그인
    public IntegrationLoginResponse integratedLogin(IntegrationLoginRequest loginRequest, HttpServletResponse response) {
        log.debug("로그인 요청: {}, 사용자 유형: {}", loginRequest.getIntegratedAccount(), loginRequest.getLoginType());

        Role role = getRoleFromLoginType(loginRequest.getLoginType());
        UserDetails userDetails = customUserDetailsService.loadUserByAccountAndRole(loginRequest.getIntegratedAccount(), role);

        // 비밀번호 검증
        if (!userDetails.getPassword().equals(loginRequest.getIntegratedPw())) {
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }

        // 클럽 ID 설정 (리더의 경우)
        Long clubId = null;
        if (userDetails instanceof CustomLeaderDetails) {
            clubId = ((CustomLeaderDetails) userDetails).getClubId();
        }

        // 토큰 생성
        String accessToken = jwtProvider.createAccessToken(userDetails.getUsername());
        String refreshToken = jwtProvider.createRefreshToken(userDetails.getUsername(), response);


        log.debug("로그인 성공, uuid: {}", userDetails.getUsername());
        return new IntegrationLoginResponse(accessToken, refreshToken, role, clubId);
    }

    // 로그인 타입
    private Role getRoleFromLoginType(LoginType loginType) {
        return switch (loginType) {
            case LEADER -> Role.LEADER;
            case ADMIN -> Role.ADMIN;
        };
    }

    // 동아리 회장, 동연회-개발자, 사용자 통합 로그아웃
    public void integratedLogout(HttpServletRequest request, HttpServletResponse response) {

        // Refresh Token 추출
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        if (refreshToken != null && jwtProvider.validateRefreshToken(refreshToken)) {
            // Redis에서 Refresh Token 삭제
            jwtProvider.deleteRefreshToken(refreshToken);
            log.debug("로그아웃: 리프레시 토큰 삭제 완료");
        }

        // 클라이언트의 쿠키에서 리프레시 토큰 삭제
        jwtProvider.deleteRefreshTokenCookie(response);

        log.debug("로그아웃 성공");
    }
}