package com.USWCicrcleLink.server.admin.notice.service;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeCreationRequest;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeDetailResponse;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponse;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponseAssembler;
import com.USWCicrcleLink.server.admin.notice.repository.NoticeRepository;
import com.USWCicrcleLink.server.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
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
    private final NoticeListResponseAssembler noticeListResponseAssembler;
    private final AdminRepository adminRepository;

    //공지사항 전체 리스트 조회
    public List<NoticeListResponse> getAllNotices() {
        return noticeRepository.findAll().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    //공지사항 리스트 조회(페이징)
    public PagedModel<NoticeListResponse> getNotices(Pageable pageable, PagedResourcesAssembler<Notice> pagedResourcesAssembler) {
        Page<Notice> noticePage = noticeRepository.findAll(pageable);
        return pagedResourcesAssembler.toModel(noticePage, noticeListResponseAssembler);
    }


    //공지사항 내용 조회
    public NoticeDetailResponse getNoticeById(Long id) {
        return noticeRepository.findById(id)
                .map(this::convertToDetailResponse)
                .orElse(null);
    }

    //공지사항 생성
    public NoticeDetailResponse createNotice(NoticeCreationRequest request, Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));

        Notice notice = Notice.builder()
                .noticeTitle(request.getNoticeTitle())
                .noticeContent(request.getNoticeContent())
                .noticePhotos(request.getNoticePhotos())
                .noticeCreatedAt(LocalDateTime.now())
                .admin(admin)
                .build();
        Notice savedNotice = noticeRepository.save(notice);
        return convertToDetailResponse(savedNotice);
    }


    //공지사항 수정
    public NoticeDetailResponse updateNotice(Long id, NoticeCreationRequest request) {
        Notice notice = noticeRepository.findById(id).orElse(null);
        if (notice != null) {
            if (request.getNoticeTitle() != null) {
                notice.setNoticeTitle(request.getNoticeTitle());
            }
            if (request.getNoticeContent() != null) {
                notice.setNoticeContent(request.getNoticeContent());
            }
            if (request.getNoticePhotos() != null) {
                notice.setNoticePhotos(request.getNoticePhotos());
            }
            Notice updatedNotice = noticeRepository.save(notice);
            return convertToDetailResponse(updatedNotice);
        }
        return null;
    }

    //공지사항 삭제
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }
    
    //공지사항 상세 내용
    private NoticeDetailResponse convertToDetailResponse(Notice notice) {
        return NoticeDetailResponse.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .noticeCreatedAt(notice.getNoticeCreatedAt())
                .build();
    }
    
    //공지사항 리스트
    private NoticeListResponse convertToListResponse(Notice notice) {
        return NoticeListResponse.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .adminName(notice.getAdmin().getAdminName())
                .noticeCreatedAt(notice.getNoticeCreatedAt())
                .build();
    }
}
