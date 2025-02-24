package com.USWCicrcleLink.server.global.security.Integration.service;

import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.security.jwt.dto.TokenDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminLeaderAuthService {
    private final JwtProvider jwtProvider;

    /**
     * Admin & Leader 토큰 재발급 (UUID 기반)
     */
    public TokenDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);
        if (refreshToken == null) {
            return forceLogout(response, "리프레시 토큰 없음 - 로그아웃 처리 진행");
        }

        if (!jwtProvider.validateRefreshToken(refreshToken, false)) {
            return forceLogout(response, "유효하지 않은 리프레시 토큰 감지 - 로그아웃 처리 진행");
        }

        UUID uuid = jwtProvider.getUUIDFromRefreshToken(refreshToken, false);
        log.debug("리프레시 토큰 검증 완료 - UUID: {}", uuid);

        jwtProvider.deleteRefreshToken(uuid);
        String newAccessToken = jwtProvider.createAccessToken(uuid, response);
        String newRefreshToken = jwtProvider.createRefreshToken(uuid, response, false);

        log.debug("토큰 갱신 성공 - UUID: {}", uuid);
        return new TokenDto(newAccessToken, newRefreshToken);
    }

    /**
     * Admin & Leader 로그아웃
     */
    public void adminLeaderLogout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);
        if (refreshToken == null) {
            forceLogout(response, "로그아웃 실패 - 리프레시 토큰 없음");
            return;
        }

        if (!jwtProvider.validateRefreshToken(refreshToken, false)) {
            forceLogout(response, "유효하지 않은 리프레시 토큰 - 로그아웃 계속 진행");
            return;
        }

        UUID uuid = jwtProvider.getUUIDFromRefreshToken(refreshToken, false);
        log.debug("로그아웃 진행 - UUID: {}", uuid);

        jwtProvider.blacklistRefreshToken(refreshToken, false);
        jwtProvider.deleteRefreshToken(uuid);

        forceLogout(response, "로그아웃 성공 - UUID: " + uuid);
    }

    /**
     * 로그아웃 처리 (쿠키 삭제 및 로그)
     */
    private TokenDto forceLogout(HttpServletResponse response, String logMessage) {
        log.warn(logMessage);
        jwtProvider.deleteRefreshTokenCookie(response);
        log.debug("리프레시 토큰 무효화 - 로그아웃 처리 완료");
        return null;
    }
}