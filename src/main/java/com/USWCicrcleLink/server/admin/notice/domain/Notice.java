package com.USWCicrcleLink.server.admin.notice.domain;

import com.USWCicrcleLink.server.admin.domain.Admin;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "NOTICE_TABLE")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "notice_title")
    private String noticeTitle;

    @Column(name = "notice_content")
    private String noticeContent;

    @ElementCollection
    @CollectionTable(name = "notice_photos", joinColumns = @JoinColumn(name = "notice_id"))
    @Column(name = "photo_path")
    private List<String> noticePhotos;

    @Column(name = "notice_created_at")
    private LocalDateTime noticeCreatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;
}