package com.USWCicrcleLink.server.clubLeader.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// FCM 실제 전송 데이터
@Getter
@Builder
public class FcmMessageDto {
    private boolean validateOnly;// 유효성 검사
    private Message message;

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Message {
        private Notification notification;
        private String token;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Notification {
        private String title;
        private String body;
        private String image;
    }

}
