package com.USWCicrcleLink.server.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLoginRequest {
    private String adminAccount;
    private String adminPw;
}
