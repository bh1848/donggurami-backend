package com.USWCicrcleLink.server.global.login.api;

import com.USWCicrcleLink.server.global.login.dto.IntegratedLoginRequest;
import com.USWCicrcleLink.server.global.login.dto.IntegratedLoginResponse;
import com.USWCicrcleLink.server.global.login.service.IntegratedLoginService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/integration")
@RequiredArgsConstructor
public class IntegratedLoginController {

    private final IntegratedLoginService integratedLoginService;

    // 동아리 회장, 동연회-개발자 통합 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<IntegratedLoginResponse>> loginAdmin(@RequestBody IntegratedLoginRequest loginRequest) {
        IntegratedLoginResponse tokenDto = integratedLoginService.integratedLogin(loginRequest);
        ApiResponse<IntegratedLoginResponse> response = new ApiResponse<>("로그인 성공", tokenDto);
        return ResponseEntity.ok(response);
    }
}
