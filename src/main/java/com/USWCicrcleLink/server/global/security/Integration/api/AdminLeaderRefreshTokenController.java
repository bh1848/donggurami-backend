package com.USWCicrcleLink.server.global.security.Integration.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.Integration.service.AdminLeaderRefreshTokenService;
import com.USWCicrcleLink.server.global.security.jwt.dto.TokenDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AdminLeaderRefreshTokenController {

    private final AdminLeaderRefreshTokenService integrationRefreshTokenService;

    /**
     * 토큰 재발급
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenDto>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        TokenDto tokenDto = integrationRefreshTokenService.refreshToken(request, response);

        if (tokenDto != null) {
            ApiResponse<TokenDto> apiResponse = new ApiResponse<>("새로운 엑세스 토큰과 리프레시 토큰이 발급되었습니다. 로그인됐습니다.", tokenDto);
            return ResponseEntity.ok(apiResponse);
        } else {
            ApiResponse<TokenDto> apiResponse = new ApiResponse<>("리프레시 토큰이 유효하지 않습니다. 로그아웃됐습니다.");
            return ResponseEntity.status(401).body(apiResponse);
        }
    }
}
