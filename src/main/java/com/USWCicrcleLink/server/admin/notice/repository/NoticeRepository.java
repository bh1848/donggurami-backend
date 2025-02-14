package com.USWCicrcleLink.server.admin.notice.repository;

import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("SELECT new com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponse(n.noticeUUID, n.noticeTitle, n.admin.adminName, n.noticeCreatedAt) " +
            "FROM Notice n")
    Page<NoticeListResponse> findAllNotices(Pageable pageable);
    Optional<Notice> findByNoticeUUID(UUID noticeUUID);
}
