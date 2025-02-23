package com.USWCicrcleLink.server.global.security.service;

import com.USWCicrcleLink.server.global.security.dto.TokenDto;
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
public class AuthService {

    private final JwtProvider jwtProvider;

    /**
     * 토큰 재발급
     */
    public TokenDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        if (refreshToken == null) {
            log.warn("리프레시 토큰 없음 - 로그아웃 처리 진행");
            jwtProvider.deleteRefreshTokenCookie(response);
            return null;
        }

        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            log.warn("유효하지 않은 리프레시 토큰 감지 - 로그아웃 처리 진행");
            jwtProvider.deleteRefreshTokenCookie(response);
            return null;
        }

        UUID uuid = jwtProvider.getUUIDFromRefreshToken(refreshToken);
        log.debug("리프레시 토큰 검증 완료 - UUID: {}", uuid);

        // 기존 리프레시 토큰 삭제 (보안 강화)
        jwtProvider.deleteRefreshTokensByUuid(uuid);

        // 새로운 액세스 토큰 및 리프레시 토큰 생성
        String newAccessToken = jwtProvider.createAccessToken(uuid, response);
        String newRefreshToken = jwtProvider.createRefreshToken(uuid, response);

        log.info("토큰 갱신 성공 - UUID: {}", uuid);
        return new TokenDto(newAccessToken, newRefreshToken);
    }
}