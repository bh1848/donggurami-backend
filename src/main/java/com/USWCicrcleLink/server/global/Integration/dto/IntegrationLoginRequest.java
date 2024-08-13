package com.USWCicrcleLink.server.global.Integration.dto;

import com.USWCicrcleLink.server.global.Integration.domain.LoginType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationLoginRequest {

    private String IntegratedAccount;

    private String IntegratedPw;

    private LoginType loginType;
}
