package com.USWCicrcleLink.server.global.Integration.api;

import com.USWCicrcleLink.server.global.Integration.dto.IntegrationLoginRequest;
import com.USWCicrcleLink.server.global.Integration.dto.IntegrationLoginResponse;
import com.USWCicrcleLink.server.global.Integration.service.IntegrationService;
import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

    // 동아리 회장, 동연회-개발자, 사용자 통합 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> integrationLogout(HttpServletRequest request, HttpServletResponse response) {
        integrationService.integrationLogout(request, response);
        return ResponseEntity.ok(new ApiResponse<>("로그아웃 성공"));
    }
}
