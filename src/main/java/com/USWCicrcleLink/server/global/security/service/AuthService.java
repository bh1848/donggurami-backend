package com.USWCicrcleLink.server.global.security.service;

import com.USWCicrcleLink.server.global.security.dto.TokenDto;
import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtProvider jwtProvider;

    public TokenDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 리프레시 토큰 추출
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        // 유효성 검증 및 처리
        if (refreshToken != null && jwtProvider.validateRefreshToken(refreshToken)) {
            // 유효한 리프레시 토큰인 경우, UUID 추출
            String uuid = jwtProvider.getUUIDFromRefreshToken(refreshToken);

            // 새로운 액세스 토큰 및 리프레시 토큰 생성
            String newAccessToken = jwtProvider.createAccessToken(uuid, response);
            String newRefreshToken = jwtProvider.createRefreshToken(uuid, response);

            return new TokenDto(newAccessToken, newRefreshToken);
        } else {
            // 리프레시 토큰이 유효하지 않은 경우 null 반환
            log.debug("리프레시 토큰이 존재하지 않거나 유효하지 않음. 로그아웃 처리.");

            // 클라이언트의 쿠키에서 리프레시 토큰 삭제
            jwtProvider.deleteRefreshTokenCookie(response);

            return null; // 유효하지 않은 경우 null 반환
        }
    }
}
