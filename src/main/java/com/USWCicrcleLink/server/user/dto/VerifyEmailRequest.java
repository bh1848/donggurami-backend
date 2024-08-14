package com.USWCicrcleLink.server.user.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class VerifyEmailRequest {
    private UUID emailToken_uuid;
    private String account;
}
