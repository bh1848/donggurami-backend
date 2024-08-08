package com.USWCicrcleLink.server.global.login.dto;

import com.USWCicrcleLink.server.global.security.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegratedLoginResponse {

    private String accessToken;

    private Role role;
}
