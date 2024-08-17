package com.USWCicrcleLink.server.user.dto;

import com.USWCicrcleLink.server.global.security.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogInRequest {

    private String account;
    private String password;
    private String fcmToken;
    private Role role;
}
