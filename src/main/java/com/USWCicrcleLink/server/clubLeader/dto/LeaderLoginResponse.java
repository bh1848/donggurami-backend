package com.USWCicrcleLink.server.clubLeader.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LeaderLoginResponse {
    private String accessToken;

    private String refreshToken;

    private UUID clubUUID;

    private Boolean isAgreedTerms;
}