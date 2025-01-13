package com.USWCicrcleLink.server.admin.notice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeListResponse {
    private Long noticeId;
    private String noticeTitle;
    private String adminName;
    private LocalDateTime noticeCreatedAt;
}
