package com.USWCicrcleLink.server.admin.notice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
@Builder
@Getter
@Setter
public class NoticeListResponse extends RepresentationModel<NoticeListResponse> {
    private Long noticeId;
    private String noticeTitle;
    private String adminName;
    private LocalDateTime noticeCreatedAt;
}