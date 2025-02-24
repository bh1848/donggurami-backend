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
import org.springframework.security.core.parameters.P;
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
    public ResponseEntity<ApiResponse<Void>> userLogout(HttpServletRequest request, HttpServletResponse response) {
        userAuthService.userLogout(request, response);
        return ResponseEntity.ok(new ApiResponse<>("로그아웃 성공"));
    }

    /**
     * 토큰 재발급
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenDto>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        TokenDto tokenDto = userAuthService.refreshToken(request, response);

        if (tokenDto != null) {
            ApiResponse<TokenDto> apiResponse = new ApiResponse<>("새로운 엑세스 토큰과 리프레시 토큰이 발급되었습니다. 로그인됐습니다.", tokenDto);
            return ResponseEntity.ok(apiResponse);
        } else {
            ApiResponse<TokenDto> apiResponse = new ApiResponse<>("리프레시 토큰이 유효하지 않습니다. 로그아웃됐습니다.");
            return ResponseEntity.status(401).body(apiResponse);
        }
    }
}