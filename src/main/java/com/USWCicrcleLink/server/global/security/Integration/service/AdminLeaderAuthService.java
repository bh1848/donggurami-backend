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
     * 토큰 재발급 (Admin & Leader)
     */
    public TokenDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);
        if (refreshToken == null || !jwtProvider.validateRefreshToken(refreshToken, false)) {
            forceLogout(response);
            return null;
        }

        UUID uuid = jwtProvider.getUUIDFromRefreshToken(refreshToken, false);

        jwtProvider.deleteRefreshToken(uuid);

        String newAccessToken = jwtProvider.createAccessToken(uuid, response);
        String newRefreshToken = jwtProvider.createRefreshToken(uuid, response, false);

        log.debug("토큰 갱신 성공 - UUID: {}", uuid);
        return new TokenDto(newAccessToken, newRefreshToken);
    }

    /**
     * 로그아웃 (Admin & Leader)
     */
    public void adminLeaderLogout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        if (refreshToken == null || !jwtProvider.validateRefreshToken(refreshToken, false)) {
            log.debug("Admin/Leader 로그아웃 - 리프레시 토큰 없음 또는 검증 실패");
            forceLogout(response);
            return;
        }

        UUID uuid = jwtProvider.getUUIDFromRefreshToken(refreshToken, false);
        jwtProvider.deleteRefreshToken(uuid);
        log.debug("Admin/Leader 로그아웃 성공 - UUID: {}", uuid);

        forceLogout(response);
    }

    /**
     * 로그아웃 처리 (쿠키 삭제 및 로그)
     */
    private void forceLogout(HttpServletResponse response) {
        jwtProvider.deleteRefreshTokenCookie(response);
    }
}