package com.USWCicrcleLink.server.global.security.filter;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.JwtException;
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
import java.util.Arrays;
import java.util.List;

/**
 * JWT의 유효성을 검증하는 필터 클래스
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    // `permitAll`로 설정된 경로 리스트
    private static final List<String> PERMIT_ALL_PATHS = Arrays.asList(
            "/users/login", // 모바일 로그인
            "/users/temporary",
            "/users/email/verify-token",
            "/users/finish-signup",
            "/users/verify-duplicate/{account}",
            "/users/validate-passwords-match",
            "/users/find-account/{email}",
            "/users/auth/send-code",
            "/users/auth/verify-token",
            "/users/reset-password",
            "/users/email/resend-confirmation",
            "/auth/refresh-token", // 토큰 재발급
            "/integration/login", // 동아리 회장, 동연회-개발자 통합 로그인
            "/mainPhoto/**",
            "/introPhoto/**",
            "/my-notices/**",
            "/clubs/**",
            "/integration/logout"
    );
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();

        // `permitAll` 경로에 대해서는 필터를 적용하지 않음
        if (PERMIT_ALL_PATHS.contains(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청에서 JWT 액세스 토큰 추출
        String accessToken = jwtProvider.resolveAccessToken(request);

        if (accessToken != null) {
            if (jwtProvider.validateAccessToken(accessToken)) {
                log.debug("엑세스 토큰이 유효함: {}", accessToken);

                // 토큰으로부터 인증 정보 생성
                Authentication auth = jwtProvider.getAuthentication(accessToken);
                log.debug("인증 정보 생성: {}", auth);

                // SecurityContextHolder에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("SecurityContextHolder에 인증 정보 설정: {}", auth.getName());
            } else {

                // 토큰이 유효하지 않은 경우
                throw new JwtException(ExceptionType.INVALID_ACCESS_TOKEN);
            }
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}