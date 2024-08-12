package com.USWCicrcleLink.server.global.login.dto;

import com.USWCicrcleLink.server.global.login.domain.LoginType;
import lombok.Data;

@Data
public class IntegratedLoginRequest {

    private String IntegratedAccount;

    private String IntegratedPw;

    private LoginType loginType;
}
