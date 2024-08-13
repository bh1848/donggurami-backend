package com.USWCicrcleLink.server.global.Integration.api;

import com.USWCicrcleLink.server.global.Integration.dto.IntegrationLoginRequest;
import com.USWCicrcleLink.server.global.Integration.dto.IntegrationLoginResponse;
import com.USWCicrcleLink.server.global.Integration.service.IntegrationService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/integration")
@RequiredArgsConstructor
public class IntegrationController {

    private final IntegrationService integrationService;

    // 동아리 회장, 동연회-개발자 통합 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<IntegrationLoginResponse>> loginAdmin(@RequestBody IntegrationLoginRequest loginRequest, HttpServletResponse response) {
        IntegrationLoginResponse tokenDto = integrationService.integratedLogin(loginRequest, response);
        ApiResponse<IntegrationLoginResponse> apiResponse = new ApiResponse<>("로그인 성공", tokenDto);
        return ResponseEntity.ok(apiResponse);
    }

    // 동아리 회장, 동연회-개발자, 사용자 통합 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
        integrationService.integratedLogout(request, response);
        ApiResponse<String> apiResponse = new ApiResponse<>("로그아웃 성공");
        return ResponseEntity.ok(apiResponse);
    }
}
