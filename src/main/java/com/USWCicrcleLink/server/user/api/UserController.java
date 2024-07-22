package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import com.USWCicrcleLink.server.user.dto.UpdatePwRequest;
import com.USWCicrcleLink.server.user.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/update-userPw")
    public ApiResponse<String> updateUserPw(@RequestParam UUID uuid, @RequestBody UpdatePwRequest request) {

        userService.updatePW(uuid, request.getNewPassword(), request.getConfirmNewPassword());

        return new ApiResponse<>("비밀번호가 성공적으로 업데이트 되었습니다.");
    }

    // 임시 회원 등록
    @PostMapping("/sign-up")
    public ResponseEntity<UserTemp> SignUp(@Valid @RequestBody SignUpRequest request) {

        // 임시 회원 등록
        UserTemp userTemp = userService.signUpUserTemp(request);
        log.info("회원이메일 = {}  :  임시 회원 등록 완료", request.getEmail());

        return new ResponseEntity<>(userTemp, HttpStatus.OK);

    }
}