package com.USWCicrcleLink.server.global.security.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.dto.TokenDto;
import com.USWCicrcleLink.server.global.security.service.AuthService;
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
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenDto>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        TokenDto tokenDto = authService.refreshToken(request, response);
        ApiResponse<TokenDto> apiResponse = new ApiResponse<>("새로운 엑세스 토큰과 리프레시 토큰이 발급됐습니다.", tokenDto);
        return ResponseEntity.ok(apiResponse);
    }
}
