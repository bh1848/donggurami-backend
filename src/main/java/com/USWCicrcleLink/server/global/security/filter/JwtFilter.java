package com.USWCicrcleLink.server.global.security.filter;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.JwtException;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
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
import java.util.List;

/**
 * JWT 유효성 검증 필터
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final List<String> permitAllPaths;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();

        if (isPermitAllPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청에서 JWT 액세스 토큰 추출
        String accessToken = jwtProvider.resolveAccessToken(request);

        if (accessToken != null) {
            if (jwtProvider.validateAccessToken(accessToken)) {
                log.debug("유효한 액세스 토큰 - 요청 경로: {}", requestPath);

                Authentication auth = jwtProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("SecurityContextHolder에 인증 정보 설정 - 사용자: {}", auth.getName());
            } else {
                log.warn("유효하지 않은 액세스 토큰 감지 - 요청 경로: {}", requestPath);
                throw new JwtException(ExceptionType.INVALID_ACCESS_TOKEN);
            }
        } else {
            log.debug("액세스 토큰 없음 - 요청 경로: {}", requestPath);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPermitAllPath(String requestPath) {
        return permitAllPaths.contains(requestPath);
    }
}
