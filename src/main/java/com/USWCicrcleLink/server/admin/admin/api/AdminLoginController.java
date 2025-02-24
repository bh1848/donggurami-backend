package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginRequest;
import com.USWCicrcleLink.server.admin.admin.service.AdminLoginService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.jwt.dto.TokenDto;
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
public class AdminLoginController {
    private final AdminLoginService adminLoginService;

    /**
     * 로그인 (Admin)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenDto>> adminLogin(@RequestBody @Valid AdminLoginRequest request, HttpServletResponse httpServletResponse){
        TokenDto tokenDto = adminLoginService.adminLogin(request,httpServletResponse);
        return ResponseEntity.ok(new ApiResponse<>("운영팀 로그인 성공", tokenDto));
    }
}
