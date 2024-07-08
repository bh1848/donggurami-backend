package com.USWCicrcleLink.server.notice.service;

import com.USWCicrcleLink.server.notice.domain.Notice;
import com.USWCicrcleLink.server.notice.dto.NoticeResponse;
import com.USWCicrcleLink.server.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    //공지사항 목록 조회
    public List<NoticeResponse> getAllNotices() {
        return noticeRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    //공지사항 페이징 목록 조회
    public Page<NoticeResponse> getNotices(Pageable pageable) {
        return noticeRepository.findAll(pageable).map(this::convertToResponse);
    }

    //공지사항 내용 조회
    public NoticeResponse getNoticeById(Long id) {
        return noticeRepository.findById(id)
                .map(this::convertToResponse)
                .orElse(null);
    }

    //공지사항 생성
    public NoticeResponse createNotice(Notice noticeRequest) {
        Notice notice = Notice.builder()
                .noticeTitle(noticeRequest.getNoticeTitle())
                .noticeContent(noticeRequest.getNoticeContent())
                .noticeCreatedAt(LocalDateTime.now())
                .build();
        Notice savedNotice = noticeRepository.save(notice);
        return convertToResponse(savedNotice);
    }

    //공지사항 수정
    public NoticeResponse updateNotice(Long id, Notice noticeRequest) {
        Notice notice = noticeRepository.findById(id).orElse(null);
        if (notice != null) {
            notice.setNoticeTitle(noticeRequest.getNoticeTitle());
            notice.setNoticeContent(noticeRequest.getNoticeContent());
            notice.setNoticeUpdatedAt(LocalDateTime.now());
            Notice updatedNotice = noticeRepository.save(notice);
            return convertToResponse(updatedNotice);
        }
        return null;
    }

    //공지사항 삭제
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    private NoticeResponse convertToResponse(Notice notice) {
        return NoticeResponse.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .noticeCreatedAt(notice.getNoticeCreatedAt())
                .noticeUpdatedAt(notice.getNoticeUpdatedAt())
                .build();
    }
}