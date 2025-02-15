package com.USWCicrcleLink.server.admin.notice.domain;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "NOTICE_TABLE")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "notice_uuid", unique = true, nullable = false, updatable = false)
    private UUID noticeUUID;

    @Column(name = "notice_title", length = 100, nullable = false)
    private String noticeTitle;

    @Column(name = "notice_content", length=3000, nullable = false)
    private String noticeContent;

    @Column(name = "notice_created_at", updatable = false)
    private LocalDateTime noticeCreatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    // 자동 시간 설정
    @PrePersist
    public void prePersist() {
        if (noticeUUID == null) {
            this.noticeUUID = UUID.randomUUID();
        }
        this.noticeCreatedAt = LocalDateTime.now();
    }

    public void updateTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public void updateContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }
}