package com.USWCicrcleLink.server.notice.service;

import com.USWCicrcleLink.server.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }

    public Notice getNoticeById(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }

    public Notice createNotice(Notice notice) {
        notice.setCreatedAt(LocalDateTime.now());
        return noticeRepository.save(notice);
    }

    public Notice updateNotice(Long id, Notice noticeDetails) {
        Notice notice = noticeRepository.findById(id).orElse(null);
        if (notice != null) {
            notice.setTitle(noticeDetails.getTitle());
            notice.setContent(noticeDetails.getContent());
            return noticeRepository.save(notice);
        }
        return null;
    }

    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }
}

