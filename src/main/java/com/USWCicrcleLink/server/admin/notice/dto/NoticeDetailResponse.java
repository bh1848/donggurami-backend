package com.USWCicrcleLink.server.admin.notice.dto;

import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeDetailResponse {
    private UUID noticeUUID;
    private String noticeTitle;
    private String noticeContent;
    private List<String> noticePhotos;
    private LocalDateTime noticeCreatedAt;
    private String adminName;

    public static NoticeDetailResponse from(Notice notice, List<String> noticePhotoPath) {
        return NoticeDetailResponse.builder()
                .noticeUUID(notice.getNoticeUUID())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .noticePhotos(noticePhotoPath)
                .noticeCreatedAt(notice.getNoticeCreatedAt())
                .adminName(notice.getAdmin().getAdminName())
                .build();
    }
}