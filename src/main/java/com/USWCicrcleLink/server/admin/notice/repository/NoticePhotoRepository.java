package com.USWCicrcleLink.server.admin.notice.repository;

import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import com.USWCicrcleLink.server.admin.notice.domain.NoticePhoto;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoticePhotoRepository extends JpaRepository<NoticePhoto, Long> {
    List<NoticePhoto> findByNotice(Notice notice);
}
