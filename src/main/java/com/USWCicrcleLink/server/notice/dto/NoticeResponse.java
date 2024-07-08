package com.USWCicrcleLink.server.notice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NoticeResponse {
    private Long noticeId;
    private String noticeTitle;
    private String noticeContent;
    private LocalDateTime noticeCreatedAt;
    private LocalDateTime noticeUpdatedAt;
}
