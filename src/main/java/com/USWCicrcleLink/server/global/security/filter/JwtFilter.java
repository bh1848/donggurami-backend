package com.USWCicrcleLink.server.global.security.filter;

import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Jwt가 유효성을 검증하는 Filter
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // 요청에서 JWT 토큰을 추출
        String token = jwtProvider.resolveAccessToken(request);

        // 토큰이 유효한지 확인
        if (token != null && jwtProvider.validateAccessToken(token)) {
            // 인증 정보 설정
            setAuthentication(token, request);
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    // SecurityContext에 인증 정보 설정
    private void setAuthentication(String token, HttpServletRequest request) {
        // 토큰에서 사용자 UUID 추출
        String userUUID = jwtProvider.getUserUUID(token);
        // 사용자 세부 정보 로드
        UserDetails userDetails = jwtProvider.getUserDetails(userUUID);

        // 사용자 세부 정보가 있는 경우 SecurityContext에 설정
        if (userDetails != null) {
            // 인증 토큰 생성
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            // 요청 세부 정보 설정
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}