package com.USWCicrcleLink.server.user.dto;

import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyNoticeResponse {
    private Long noticeId;
    private String noticeTitle;
    private String adminName;
    private LocalDateTime noticeCreatedAt;

    public static MyNoticeResponse from(Notice notice) {
        return new MyNoticeResponse(
                notice.getNoticeId(),
                notice.getNoticeTitle(),
                notice.getAdmin().getAdminName(),
                notice.getNoticeCreatedAt()
        );
    }
}
