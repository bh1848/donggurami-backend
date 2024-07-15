package com.USWCicrcleLink.server.admin.notice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NoticeCreationRequest {
    private String noticeTitle;
    private String noticeContent;
    private List<String> noticePhotos;
}