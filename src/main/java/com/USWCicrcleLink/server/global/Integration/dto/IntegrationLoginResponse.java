package com.USWCicrcleLink.server.global.Integration.dto;

import com.USWCicrcleLink.server.global.security.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationLoginResponse {

    private String accessToken;

    private String refreshToken;

    private Role role;

    private Long clubId;
}
