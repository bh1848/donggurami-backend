package com.USWCicrcleLink.server.notice.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class NoticeCreationRequest {
    private String noticeTitle;
    private String noticeContent;
    private List<MultipartFile> noticePhoto; // 이미지 파일 리스트 추가
}
