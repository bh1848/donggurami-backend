package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.jwt.dto.TokenDto;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import com.USWCicrcleLink.server.user.dto.LogInRequest;
import com.USWCicrcleLink.server.user.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserAuthService userAuthService;

    /**
     * User 로그인
     */
    @PostMapping("/login")
    @RateLimite(action = "APP_LOGIN")
    public ResponseEntity<ApiResponse<TokenDto>> userLogin(@RequestBody @Validated(ValidationSequence.class) LogInRequest request, HttpServletResponse response) {
        userAuthService.verifyLogin(request);
        TokenDto tokenDto = userAuthService.userLogin(request, response);
        return ResponseEntity.ok(new ApiResponse<>("로그인 성공", tokenDto));
    }

    /**
     * User 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> userLogout(HttpServletRequest request, HttpServletResponse response) {
        userAuthService.userLogout(request, response);
        return ResponseEntity.ok().build();
    }
}