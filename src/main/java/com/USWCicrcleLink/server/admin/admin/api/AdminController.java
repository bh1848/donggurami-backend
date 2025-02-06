package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginRequest;
import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginResponse;
import com.USWCicrcleLink.server.admin.admin.service.AdminService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admins")
public class AdminController {
    private final AdminService adminService;

    // 운영팀 로그인(웹)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> adminLogin(@RequestBody AdminLoginRequest request, HttpServletResponse httpServletResponse){
        AdminLoginResponse adminLoginResponse = adminService.adminLogin(request,httpServletResponse);
        ApiResponse<AdminLoginResponse> response = new ApiResponse<>("운영팀 로그인 성공", adminLoginResponse);
        return ResponseEntity.ok(response);
    }
}
