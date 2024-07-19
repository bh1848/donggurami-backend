package com.USWCicrcleLink.server.user.api;


import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import com.USWCicrcleLink.server.user.dto.UpdatePwRequest;
import com.USWCicrcleLink.server.user.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
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
    public ResponseEntity<String> updateUserPw(@RequestHeader("userUUID") UUID UserUUID, @RequestBody UpdatePwRequest request) {

        userService.updatePW(UserUUID, request.getNewPassword(), request.getConfirmNewPassword());

        return ResponseEntity.ok("비밀번호가 성공적으로 업데이트 되었습니다.");
    }

    // 임시 회원 등록 및 인증 메일 전송
    @PostMapping("/temp-sign-up")
    public ResponseEntity<ApiResponse> tempSignUp(@Valid @RequestBody SignUpRequest request) throws MessagingException {

        UserTemp userTemp = userService.signUpUserTemp(request);
        UUID emailTokenId = userService.sendEmail(userTemp);
        ApiResponse response = new ApiResponse("인증 메일 전송 완료",emailTokenId);

        return new ResponseEntity<>(response, HttpStatus.OK);

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

    // 이메일 인증 확인 후 회원가입
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam @Valid  UUID emailTokenId){
        UserTemp userTemp = userService.checkEmailToken(emailTokenId);
        User signUpUser = userService.signUpUser(userTemp);
        ApiResponse response = new ApiResponse( "회원 가입 완료",signUpUser);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }







}