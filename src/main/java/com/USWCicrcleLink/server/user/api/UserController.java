package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailTokenService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.user.domain.AuthToken;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.*;
import com.USWCicrcleLink.server.user.service.AuthTokenService;
import com.USWCicrcleLink.server.user.service.UserService;

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
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final EmailTokenService emailTokenService;

    @PatchMapping("/{uuid}/userpw")
    public ApiResponse<String> updateUserPw(@PathVariable UUID uuid, @RequestBody UpdatePwRequest request) {

        userService.updateNewPW(uuid, request.getUserPw(),request.getNewPw(), request.getConfirmNewPw());

        return new ApiResponse<>("비밀번호가 성공적으로 업데이트 되었습니다.");
    }

    // 임시 회원 등록 및 인증 메일 전송
    @PostMapping("/temp-sign-up")
    public ResponseEntity<ApiResponse<UserTemp>> registerTemporaryUser(@Valid @RequestBody SignUpRequest request) throws MessagingException {

        UserTemp userTemp = userService.registerUserTemp(request);
        EmailToken emailToken = emailTokenService.createEmailToken(userTemp);
        userService.sendSignUpMail(userTemp,emailToken);

        ApiResponse<UserTemp> response = new ApiResponse<>("인증 메일 전송 완료",userTemp);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 이메일 인증 확인 후 회원가입
    @PostMapping("/verify/{emailTokenId}")
    public ResponseEntity<ApiResponse<User>> verifySignUp(@PathVariable  UUID emailTokenId) {

        UserTemp userTemp = userService.verifyEmailToken(emailTokenId);
        User signUpUser = userService.signUp(userTemp);

        ApiResponse<User> response = new ApiResponse<>( "회원 가입 완료",signUpUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 회원가입 시의 계정 중복 체크
    @GetMapping("/verify/duplicate/{account}")
    public ResponseEntity<ApiResponse<String>> verifyAccountDuplicate(@PathVariable String account) {

        userService.verifyAccountDuplicate(account);

        ApiResponse<String> response = new ApiResponse<>("사용 가능한 ID 입니다.", account);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 비밀번호 일치 확인
    @PostMapping("/validate-passwords-match")
    public ResponseEntity<ApiResponse<Void>> validatePasswordsMatch(@Valid @RequestBody PasswordRequest request) {

        userService.validatePasswordsMatch(request);

        return ResponseEntity.ok(new ApiResponse<>("비밀번호가 일치합니다"));
    }

    // 로그인
    @PostMapping("/log-in")
    public ResponseEntity<ApiResponse<String>> LogIn(@Valid @RequestBody LogInRequest request) {

        String account  = userService.logIn(request);

        ApiResponse<String> response = new ApiResponse<>("로그인 성공", account);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 아이디 찾기
    @GetMapping ("/find-user-account/{email}")
    ResponseEntity<ApiResponse<String>> findUserAccount(@PathVariable String email) throws MessagingException {

        User findUser= userService.findUser(email);
        userService.sendAccountInfoMail(findUser);

        ApiResponse<String> response = new ApiResponse<>("계정 정보 전송 완료", findUser.getUserAccount());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 인증 코드 전송
    @PostMapping("/send-auth-code")
    ResponseEntity<ApiResponse<Void>> sendAuthCode (@Valid @RequestBody UserInfoDto request) throws MessagingException {

        User user = userService.validateAccountAndEmail(request);

        AuthToken authToken = authTokenService.createAuthToken(user);
        userService.sendAuthCodeMail(user,authToken);

        ApiResponse<Void> response = new ApiResponse<>("인증코드가 전송 되었습니다");
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 인증 코드 검증
    @PostMapping("verify-auth-token/{uuid}")
    public ResponseEntity<ApiResponse<String>> verifyAuthToken(@PathVariable UUID uuid, @RequestBody UserInfoDto request) {

        authTokenService.verifyAuthToken(uuid, request);
        authTokenService.deleteAuthToken(uuid);

        ApiResponse<String> response = new ApiResponse<>("인증 코드 검증이 완료되었습니다",request.getUserAccount());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }


    // 비밀번호 재설정
    @PatchMapping("/reset-password/{uuid}")
    public ApiResponse<String> resetUserPw(@PathVariable UUID uuid, @RequestBody PasswordRequest request) {

        User user = userService.findByUuid(uuid);
        userService.resetPW(user,request);

        return new ApiResponse<>("비밀번호가 변경되었습니다.");
    }

    //이메일 재인증
    @PostMapping("/resend-confirm-email/{emailTokenId}")
    public ResponseEntity<ApiResponse<UUID>> resendConfirmEmail(@PathVariable UUID emailTokenId) throws MessagingException {

        EmailToken emailToken = emailTokenService.updateCertificationTime(emailTokenId);
        userService.sendSignUpMail(emailToken.getUserTemp(),emailToken);

        ApiResponse<UUID> response = new ApiResponse<>("이메일 재인증을 해주세요", emailTokenId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }


}