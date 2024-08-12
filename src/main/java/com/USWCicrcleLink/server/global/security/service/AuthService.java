package com.USWCicrcleLink.server.global.security.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.JwtException;
import com.USWCicrcleLink.server.global.security.dto.TokenDto;
import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;

    public TokenDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 리프레시 토큰 추출
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        // 리프레시 토큰 유효성 검사
        if (refreshToken == null || !jwtProvider.validateRefreshToken(refreshToken)) {
            throw new JwtException(ExceptionType.INVALID_REFRESH_TOKEN);
        }

        // Redis에서 저장된 UUID 가져오기
        String uuid = jwtProvider.getStoredUuidFromRefreshToken(refreshToken);

        // 유효한 리프레시 토큰인 경우: 기존 리프레시 토큰 삭제
        jwtProvider.deleteRefreshToken(refreshToken);

        // 새로운 액세스 토큰 및 리프레시 토큰 생성
        String newAccessToken = jwtProvider.createAccessToken(uuid);
        String newRefreshToken = jwtProvider.createRefreshToken(uuid, response);

        return new TokenDto(newAccessToken, newRefreshToken);
    }
}
