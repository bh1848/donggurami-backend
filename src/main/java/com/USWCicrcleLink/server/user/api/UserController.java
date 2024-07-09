package com.USWCicrcleLink.server.user.api;

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


import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/update-userPw")

    public ResponseEntity<String> updateUserPw(@RequestHeader("userUUID") UUID UserUUID, @RequestBody UpdatePwRequest request) {

        userService.updatePW(UserUUID, request.getNewPassword(), request.getConfirmNewPassword());

        return ResponseEntity.ok("비밀번호가 성공적으로 업데이트 되었습니다.");
    }

    // 임시 회원 등록
    @PostMapping("/sign-up")
    public ResponseEntity<UserTemp> SignUp(@Valid @RequestBody SignUpRequest request) {

        // 임시 회원 등록
        UserTemp userTemp = userService.signUpUserTemp(request);
        log.info("회원이메일 = {}  :  임시 회원 등록 완료", request.getEmail());

        return new ResponseEntity<>(userTemp, HttpStatus.OK);

    }

    // 임시 회원 데이터 생성
    @PostConstruct
    public void initializeDummyData() {
        createAndSignUpMember("account", "password", "suwon", "12343", "art", "email-1");
        createAndSignUpMember("admin", "1234", "suwonsuwon", "34523", "math", "email-2");
    }

    private void createAndSignUpMember(String account, String password, String userName, String studentNumber, String major, String email) {

        SignUpRequest request = new SignUpRequest();
        request.setAccount(account);
        request.setPassword(password);
        request.setUserName(userName);
        request.setStudentNumber(studentNumber);
        request.setMajor(major);
        request.setEmail(email);

        userService.signUpUserTemp(request);
    }
}