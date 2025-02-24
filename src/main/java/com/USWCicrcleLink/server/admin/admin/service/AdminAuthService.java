package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginRequest;
import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginResponse;
import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import com.USWCicrcleLink.server.global.security.details.service.CustomUserDetailsService;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 로그인 (Admin)
     */
    @RateLimite(action = "WEB_LOGIN")
    public AdminLoginResponse adminLogin(AdminLoginRequest request, HttpServletResponse response) {
        UserDetails userDetails = loadAdminDetails(request.getAdminAccount());

        if (!passwordEncoder.matches(request.getAdminPw(), userDetails.getPassword())) {
            log.warn("Admin 로그인 실패 - 비밀번호 불일치, 계정: {}", request.getAdminAccount());
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }
        log.info("Admin 로그인 성공 - 계정: {}", request.getAdminAccount());

        UUID adminUUID = extractAdminUUID(userDetails);

        String accessToken = jwtProvider.createAccessToken(adminUUID, response);
        String refreshToken = jwtProvider.createRefreshToken(adminUUID, response, Role.ADMIN);

        return new AdminLoginResponse(accessToken, refreshToken, Role.ADMIN);
    }

    // Admin UUID 추출 (CustomAdminDetails에서 가져옴)
    private UUID extractAdminUUID(UserDetails userDetails) {
        if (userDetails instanceof CustomAdminDetails customAdminDetails) {
            return customAdminDetails.admin().getAdminUUID();
        }
        throw new UserException(ExceptionType.USER_NOT_EXISTS);
    }

    // Admin 계정 확인 (Role.ADMIN인지 검증)
    private UserDetails loadAdminDetails(String account) {
        try {
            return customUserDetailsService.loadUserByAccountAndRole(account, Role.ADMIN);
        } catch (UserException e) {
            log.warn("Admin 로그인 실패 - 존재하지 않는 계정: {}", account);
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }
    }

    /**
     * 로그아웃 (Admin)
     */
    public void adminLogout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        if (refreshToken != null && jwtProvider.validateRefreshToken(refreshToken, Role.ADMIN)) {
            UUID uuid = jwtProvider.getUUIDFromRefreshToken(refreshToken, Role.ADMIN);

            // 로그아웃 시 리프레시 토큰 블랙리스트 적용 후 삭제
            jwtProvider.blacklistRefreshToken(refreshToken, Role.ADMIN);
            jwtProvider.deleteRefreshTokensByUuid(uuid);
            log.debug("Admin(ADMIN) 로그아웃 - UUID: {}", uuid);
        } else {
            log.debug("유효하지 않은 리프레시 토큰 - 로그아웃 계속 진행");
        }

        // 클라이언트 쿠키에서 리프레시 토큰 삭제
        jwtProvider.deleteRefreshTokenCookie(response);
        log.info("Admin(ADMIN) 로그아웃 성공");
    }
}