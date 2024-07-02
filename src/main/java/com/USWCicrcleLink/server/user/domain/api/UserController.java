package com.USWCicrcleLink.server.user.domain.api;

import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.dto.UpdatePwRequest;
import com.USWCicrcleLink.server.user.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PatchMapping("/update-userPw")
    public ResponseEntity<String> updateUserPw(@RequestHeader("userUUID")String UserUUID,@RequestBody UpdatePwRequest request){

        userService.updatePW(UserUUID, request.getNewPassword(), request.getConfirmNewPassword());

        return ResponseEntity.ok("비밀번호가 성공적으로 업데이트 되었습니다.");
    }

}
