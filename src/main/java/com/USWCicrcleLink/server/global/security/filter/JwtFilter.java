package com.USWCicrcleLink.server.global.security.filter;

import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT의 유효성을 검증하는 필터 클래스
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    // JWT 검증하고 유효한 경우 인증 정보 설정
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 요청에서 JWT 액세스 토큰 추출
            String accessToken = jwtProvider.resolveAccessToken(request);
            log.debug("엑세스 토큰 추출: {}", accessToken);

            // 토큰이 존재하고 유효한 경우
            if (accessToken != null && jwtProvider.validateAccessToken(accessToken)) {
                log.debug("엑세스 토큰이 유효함: {}", accessToken);

                // 토큰으로부터 인증 정보 생성
                Authentication auth = jwtProvider.getAuthentication(accessToken);
                log.debug("인증 정보 생성: {}", auth);

                // SecurityContextHolder에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("SecurityContextHolder에 인증 정보 설정: {}", auth.getName());
            } else {
                log.warn("토큰이 유효하지 않거나 비어있음");
            }
        } catch (Exception e) {
            // 예외 발생 시 에러 로그 출력
            log.error("인증되지 않은 사용자입니다: " + e.getMessage(), e);
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
