package com.USWCicrcleLink.server.clubLeader.dto;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

// 모바일에서 전달받은 객체
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmSendDto {
    private String token;

    private String title;

    private String body;

    private Map<String, String> data;

    @Builder(toBuilder = true)
    public FcmSendDto(String token, String title, String body, Map<String, String> data) {
        this.token = token;
        this.title = title;
        this.body = body;
        this.data = data;
    }
}
