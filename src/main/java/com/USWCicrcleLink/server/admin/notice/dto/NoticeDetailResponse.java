package com.USWCicrcleLink.server.admin.notice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class NoticeDetailResponse{
    private Long noticeId;
    private String noticeTitle;
    private String noticeContent;
    private List<String> noticePhotos;
    private LocalDateTime noticeCreatedAt;
}