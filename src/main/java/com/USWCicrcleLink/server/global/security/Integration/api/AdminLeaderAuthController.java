package com.USWCicrcleLink.server.global.security.Integration.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.Integration.service.AdminLeaderAuthService;
import com.USWCicrcleLink.server.global.security.jwt.dto.TokenDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AdminLeaderAuthController {
    private final AdminLeaderAuthService adminLeaderAuthService;

    /**
     * Admin & Leader 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        adminLeaderAuthService.adminLeaderLogout(request, response);
        return ResponseEntity.ok(new ApiResponse<>("로그아웃 성공"));
    }

    /**
     * Admin & Leader 토큰 갱신
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenDto>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        TokenDto tokenDto = adminLeaderAuthService.refreshToken(request, response);

        if (tokenDto == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>("토큰 갱신 실패", null));
        }
        return ResponseEntity.ok(new ApiResponse<>("토큰 갱신 성공", tokenDto));
    }
}
