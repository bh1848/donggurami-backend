package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginRequest;
import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginResponse;
import com.USWCicrcleLink.server.admin.admin.service.AdminAuthService;
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
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminAuthController {
    private final AdminAuthService adminAuthService;

    /**
     * 로그인 (Admin)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> adminLogin(@RequestBody @Valid AdminLoginRequest request, HttpServletResponse httpServletResponse){
        AdminLoginResponse adminLoginResponse = adminAuthService.adminLogin(request,httpServletResponse);
        return ResponseEntity.ok(new ApiResponse<>("운영팀 로그인 성공", adminLoginResponse));
    }

    /**
     * 로그아웃 (Admin)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        adminAuthService.adminLogout(request, response);
        return ResponseEntity.ok().build();
    }
}
