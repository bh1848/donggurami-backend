package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginRequest;
import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginResponse;
import com.USWCicrcleLink.server.global.Integration.domain.LoginType;
import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.service.CustomUserDetailsService;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
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
     * 운영팀 로그인
     */
    @RateLimite(action = "WEB_LOGIN")
    public AdminLoginResponse adminLogin(AdminLoginRequest request, HttpServletResponse response) {
        UserDetails userDetails = loadAdminDetails(request.getAdminAccount());

        if (!passwordEncoder.matches(request.getAdminPw(), userDetails.getPassword())) {
            log.warn("운영팀 로그인 실패 - 비밀번호 불일치, 계정: {}", request.getAdminAccount());
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }
        log.info("운영팀 로그인 성공 - 계정: {}", request.getAdminAccount());

        UUID adminUUID = extractAdminUUID(userDetails);

        String accessToken = jwtProvider.createAccessToken(adminUUID, response);
        String refreshToken = jwtProvider.createRefreshToken(adminUUID, response);

        return new AdminLoginResponse(accessToken, refreshToken, Role.ADMIN);
    }

    // Admin UUID 추출 (CustomAdminDetails에서 가져옴)
    private UUID extractAdminUUID(UserDetails userDetails) {
        if (userDetails instanceof CustomAdminDetails customAdminDetails) {
            return customAdminDetails.admin().getAdminUUID(); // ✅ Admin UUID 가져오기
        }
        throw new UserException(ExceptionType.USER_NOT_EXISTS);
    }

    // account 및 role 확인
    private UserDetails loadAdminDetails(String account) {
        try {
            return customUserDetailsService.loadUserByAccountAndRole(account, Role.ADMIN);
        } catch (UserException e) {
            log.warn("운영팀 로그인 실패 - 존재하지 않는 계정: {}", account);
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }
    }
}
