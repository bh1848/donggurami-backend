package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginRequest;
import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginResponse;
import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.details.service.CustomUserDetailsService;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
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
public class AdminLoginService {
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 로그인 (Admin)
     */
    @RateLimite(action = "WEB_LOGIN")
    public AdminLoginResponse adminLogin(AdminLoginRequest request, HttpServletResponse response) {
        UserDetails userDetails = customUserDetailsService.loadUserByAccountAndRole(request.getAdminAccount(), Role.ADMIN);

        if (!passwordEncoder.matches(request.getAdminPw(), userDetails.getPassword())) {
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }
        log.debug("Admin 로그인 성공 - 계정: {}", request.getAdminAccount());

        UUID adminUUID = extractAdminUUID(userDetails);

        String accessToken = jwtProvider.createAccessToken(adminUUID, response);
        String refreshToken = jwtProvider.createRefreshToken(adminUUID, response);

        return new AdminLoginResponse(accessToken, refreshToken, Role.ADMIN);
    }

    // Admin UUID 추출 (CustomAdminDetails에서 가져옴)
    private UUID extractAdminUUID(UserDetails userDetails) {
        if (userDetails instanceof CustomAdminDetails customAdminDetails) {
            return customAdminDetails.admin().getAdminUUID();
        }
        throw new UserException(ExceptionType.USER_NOT_EXISTS);
    }
}