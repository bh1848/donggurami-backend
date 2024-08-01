package com.USWCicrcleLink.server.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto {

    private String userAccount;
    private String userPw;
    private String email;
    private String authCode; // 인증 코드

}
