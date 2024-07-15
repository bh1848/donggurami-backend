package com.USWCicrcleLink.server.admin.notice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NoticeDetailResponse{
    private Long noticeId;
    private String noticeTitle;
    private String noticeContent;
    private LocalDateTime noticeCreatedAt;
}