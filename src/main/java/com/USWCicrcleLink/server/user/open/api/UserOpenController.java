package com.USWCicrcleLink.server.user.open.api;


import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import com.USWCicrcleLink.server.user.open.service.UserOpenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/open/user")
@RequiredArgsConstructor
public class UserOpenController {

    private final  UserOpenService userOpenService;

    // 이메일 인증 전 임시로 회원정보 저장하기
    @PostMapping("/sign-up")
    public ResponseEntity<UserTemp> signUp(@Valid @RequestBody SignUpRequest request){

        UserTemp findTempUser= userOpenService.signUpMemberTemp(request); // 임시 회원 정보 저장

        return new ResponseEntity<>(findTempUser, HttpStatus.OK);
    }

}
