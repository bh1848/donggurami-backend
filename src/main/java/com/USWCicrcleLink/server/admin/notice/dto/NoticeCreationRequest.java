package com.USWCicrcleLink.server.admin.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NoticeCreationRequest {
    private String noticeTitle;
    private String noticeContent;
    private List<String> noticePhotos;
}