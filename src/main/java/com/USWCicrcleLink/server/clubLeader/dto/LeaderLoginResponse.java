package com.USWCicrcleLink.server.clubLeader.dto;

import com.USWCicrcleLink.server.global.security.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LeaderLoginResponse {
    private String accessToken;

    private String refreshToken;

    private Role role;

    private Long clubId;

    private Boolean isAgreedTerms;
}
