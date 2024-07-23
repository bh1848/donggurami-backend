package com.USWCicrcleLink.server.admin.notice.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeCreationRequest {
    private String noticeTitle;
    private String noticeContent;
    private List<String> noticePhotos;
}