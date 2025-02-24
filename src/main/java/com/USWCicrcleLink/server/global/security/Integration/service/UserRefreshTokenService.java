package com.USWCicrcleLink.server.global.security.Integration.service;

import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import com.USWCicrcleLink.server.global.security.jwt.dto.TokenDto;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRefreshTokenService {
    private final JwtProvider jwtProvider;

    /**
     * User 토큰 재발급 (JWT 기반)
     */
    public TokenDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        if (refreshToken == null) {
            log.warn("리프레시 토큰 없음 - 로그아웃 처리 진행");
            forceLogout(response);
            return null;
        }

        if (!jwtProvider.validateRefreshToken(refreshToken, Role.USER)) {
            log.warn("유효하지 않은 리프레시 토큰 감지 - 로그아웃 처리 진행");
            forceLogout(response);
            return null;
        }

        UUID uuid = UUID.fromString(jwtProvider.getClaims(refreshToken).getSubject());
        log.debug("리프레시 토큰 검증 완료 - UUID: {}", uuid);

        String newAccessToken = jwtProvider.createAccessToken(uuid, response);
        String newRefreshToken = jwtProvider.createRefreshToken(uuid, response, Role.USER);

        log.info("토큰 갱신 성공 - UUID: {}", uuid);
        return new TokenDto(newAccessToken, newRefreshToken);
    }

    // 로그아웃 처리 (쿠키 삭제)
    private void forceLogout(HttpServletResponse response) {
        jwtProvider.deleteRefreshTokenCookie(response);
        log.debug("리프레시 토큰 무효화 - 로그아웃 처리 완료");
    }
}

