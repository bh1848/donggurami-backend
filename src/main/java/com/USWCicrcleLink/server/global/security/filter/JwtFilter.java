package com.USWCicrcleLink.server.global.security.filter;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.JwtException;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 유효성 검증 필터 (User, Admin, Leader UUID 처리)
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final List<String> permitAllPaths;
    private final PathMatcher pathMatcher = new AntPathMatcher();


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        if (isPermitAllPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = jwtProvider.resolveAccessToken(request);

        try {
            if (accessToken != null && jwtProvider.validateAccessToken(accessToken)) {
                Authentication auth = jwtProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(auth);

                if (auth.getPrincipal() instanceof CustomAdminDetails adminDetails) {
                    MDC.put("userType", "Admin");
                    MDC.put("userUUID", adminDetails.getAdminUUID().toString());

                    log.info("[Admin: {}, UUID: {}] SecurityContextHolder 설정 완료 - 요청 경로: {}",
                            adminDetails.getUsername(),
                            adminDetails.getAdminUUID(),
                            requestPath);
                } else if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                    MDC.put("userType", "User");
                    MDC.put("userUUID", userDetails.getUserUUID().toString());

                    log.info("[User: {}, UUID: {}] SecurityContextHolder 설정 완료 - 요청 경로: {}",
                            userDetails.getUsername(),
                            userDetails.getUserUUID(),
                            requestPath);
                } else if (auth.getPrincipal() instanceof CustomLeaderDetails leaderDetails) {
                    MDC.put("userType", "Leader");
                    MDC.put("userUUID", leaderDetails.getLeaderUUID().toString());

                    log.info("[Leader: {}, UUID: {}] SecurityContextHolder 설정 완료 - 요청 경로: {}",
                            leaderDetails.getUsername(),
                            leaderDetails.getLeaderUUID(),
                            requestPath);
                }

            } else {
                log.warn("유효하지 않은 액세스 토큰 감지 - 요청 경로: {}", requestPath);
                throw new JwtException(ExceptionType.INVALID_ACCESS_TOKEN);
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private boolean isPermitAllPath(String requestPath) {
        return permitAllPaths.stream().anyMatch(permitPath -> pathMatcher.match(permitPath, requestPath));
    }
}
