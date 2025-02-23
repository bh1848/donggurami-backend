package com.USWCicrcleLink.server.admin.notice.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import com.USWCicrcleLink.server.admin.notice.domain.NoticePhoto;
import com.USWCicrcleLink.server.admin.notice.dto.*;
import com.USWCicrcleLink.server.admin.notice.repository.NoticePhotoRepository;
import com.USWCicrcleLink.server.admin.notice.repository.NoticeRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.NoticeException;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.global.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.global.s3File.dto.S3FileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AdminNoticeService {

    private static final int FILE_LIMIT = 5; // 최대 업로드 가능한 파일 수
    private static final String S3_NOTICE_PHOTO_DIR = "noticePhoto/";
    private final NoticeRepository noticeRepository;
    private final NoticePhotoRepository noticePhotoRepository;
    private final S3FileUploadService s3FileUploadService;

    /**
     * 공지사항 리스트 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public AdminNoticePageListResponse getNotices(Pageable pageable) {
        Page<Notice> notices = noticeRepository.findAll(pageable);

        log.debug("공지사항 목록 조회 성공 - 총 {}개", notices.getTotalElements());

        List<AdminNoticeListResponse> content = notices.getContent().stream()
                .map(notice -> AdminNoticeListResponse.builder()
                        .noticeUUID(notice.getNoticeUUID())
                        .noticeTitle(notice.getNoticeTitle())
                        .adminName(notice.getAdmin().getAdminName())
                        .noticeCreatedAt(notice.getNoticeCreatedAt())
                        .build()
                ).toList();

        return AdminNoticePageListResponse.builder()
                .content(content)
                .totalPages(notices.getTotalPages())
                .totalElements(notices.getTotalElements())
                .currentPage(notices.getNumber())
                .build();
    }

    /**
     * 공지사항 조회
     */
    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeByUUID(UUID noticeUUID) {
        Notice notice = noticeRepository.findByNoticeUUID(noticeUUID)
                .orElseThrow(() -> {
                    log.warn("공지사항 조회 실패 - 존재하지 않는 UUID: {}", noticeUUID);
                    return new NoticeException(ExceptionType.NOTICE_NOT_EXISTS);
                });

        List<String> noticePhotoUrls = noticePhotoRepository.findByNotice(notice).stream()
                .sorted(Comparator.comparingInt(NoticePhoto::getOrder))
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getNoticePhotoS3Key()))
                .collect(Collectors.toList());

        log.debug("공지사항 상세 조회 성공 - ID: {}", notice.getNoticeId());

        return new NoticeDetailResponse(
                notice.getNoticeUUID(),
                notice.getNoticeTitle(),
                notice.getNoticeContent(),
                noticePhotoUrls,
                notice.getNoticeCreatedAt(),
                notice.getAdmin().getAdminName()
        );
    }

    /**
     * 공지사항 생성
     */
    public List<String> createNotice(AdminNoticeCreationRequest request, List<MultipartFile> noticePhotos) {
        Admin admin = getAuthenticatedAdmin();

        Notice notice = Notice.builder()
                .noticeTitle(request.getNoticeTitle())
                .noticeContent(request.getNoticeContent())
                .noticeCreatedAt(LocalDateTime.now())
                .admin(admin)
                .build();
        Notice savedNotice = noticeRepository.save(notice);

        // 사진 순서와 파일 개수 검증
        validatePhotoOrdersAndPhotos(request.getPhotoOrders(), noticePhotos);

        // 사진 처리
        List<String> presignedUrls = handleNoticePhotos(savedNotice, noticePhotos, request.getPhotoOrders());

        log.info("공지사항 생성 완료 - ID: {}, 첨부된 사진 수: {}", savedNotice.getNoticeId(), noticePhotos == null ? 0 : noticePhotos.size());
        return presignedUrls;
    }

    /**
     * 공지사항 수정
     */
    public List<String> updateNotice(UUID noticeUUID, AdminNoticeUpdateRequest request, List<MultipartFile> noticePhotos) {

        // 공지사항 조회
        Notice notice = noticeRepository.findByNoticeUUID(noticeUUID)
                .orElseThrow(() -> {
                    log.warn("공지사항 수정 실패 - 존재하지 않는 uuid: {}", noticeUUID);
                    return new NoticeException(ExceptionType.NOTICE_NOT_EXISTS);
                });

        notice.updateTitle(request.getNoticeTitle());
        notice.updateContent(request.getNoticeContent());

        // 기존 사진 삭제
        deleteExistingPhotos(notice);

        // 사진 순서와 파일 개수 검증
        validatePhotoOrdersAndPhotos(request.getPhotoOrders(), noticePhotos);

        // 사진 처리
        List<String> presignedUrls = handleNoticePhotos(notice, noticePhotos, request.getPhotoOrders());

        log.info("공지사항 수정 완료 - ID: {}, 첨부된 사진 수: {}", notice.getNoticeId(), noticePhotos == null ? 0 : noticePhotos.size());
        return presignedUrls;
    }

    /**
     * 공지사항 삭제
     */
    public void deleteNotice(UUID noticeUUID) {

        Notice notice = noticeRepository.findByNoticeUUID(noticeUUID)
                .orElseThrow(() -> {
                    log.warn("공지사항 삭제 실패 - 존재하지 않는 uuid: {}", noticeUUID);
                    return new NoticeException(ExceptionType.NOTICE_NOT_EXISTS);
                });

        deleteExistingPhotos(notice);

        noticeRepository.delete(notice);
        log.info("공지사항 삭제 완료 - ID: {}", notice.getNoticeId());
    }

    private Admin getAuthenticatedAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        return adminDetails.admin();
    }

    // 사진 순서와 파일 개수 검증
    private void validatePhotoOrdersAndPhotos(List<Integer> photoOrders, List<MultipartFile> noticePhotos) {
        if (noticePhotos != null && !noticePhotos.isEmpty()) {
            // 사진과 사진 순서의 개수 일치 확인
            if (photoOrders == null || noticePhotos.size() != photoOrders.size()) {
                log.warn("공지사항 사진 업로드 실패 - 사진 개수와 순서 개수 불일치");
                throw new NoticeException(ExceptionType.PHOTO_ORDER_MISMATCH);
            }

            // 사진 개수 제한 확인
            if (noticePhotos.size() > FILE_LIMIT) {
                log.warn("공지사항 사진 업로드 실패 - 최대 개수 초과 (제한: {}개, 업로드: {}개)", FILE_LIMIT, noticePhotos.size());
                throw new NoticeException(ExceptionType.UP_TO_5_PHOTOS_CAN_BE_UPLOADED);
            }
        }
    }

    // 기존 사진 일괄 삭제
    private void deleteExistingPhotos(Notice notice) {
        List<NoticePhoto> existingPhotos = noticePhotoRepository.findByNotice(notice);
        if (existingPhotos.isEmpty()) return;

        List<String> fileNames = existingPhotos.stream()
                .map(NoticePhoto::getNoticePhotoS3Key)
                .toList();

        s3FileUploadService.deleteFiles(fileNames);
        noticePhotoRepository.deleteAllInBatch(existingPhotos);

        log.debug("공지사항 사진 일괄 삭제 완료 - ID: {}, 삭제된 파일 수: {}", notice.getNoticeId(), fileNames.size());
    }

    // 사진 업로드
    private List<String> handleNoticePhotos(Notice notice, List<MultipartFile> noticePhotos, List<Integer> photoOrders) {
        if (noticePhotos == null || noticePhotos.isEmpty()) return List.of();

        List<NoticePhoto> newPhotoList = new ArrayList<>();
        List<String> presignedUrls = new ArrayList<>();

        for (int i = 0; i < noticePhotos.size(); i++) {
            MultipartFile noticePhoto = noticePhotos.get(i);
            int order = photoOrders.get(i);

            if (noticePhoto == null || noticePhoto.isEmpty()) {
                log.warn("공지사항 사진 업로드 실패 - 빈 파일 포함됨, 순서: {}", order);
                continue;
            }

            S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(noticePhoto, S3_NOTICE_PHOTO_DIR);

            NoticePhoto newPhoto = NoticePhoto.builder()
                    .notice(notice)
                    .noticePhotoName(noticePhoto.getOriginalFilename())
                    .noticePhotoS3Key(s3FileResponse.getS3FileName())
                    .order(order)
                    .build();

            newPhotoList.add(newPhoto);
            presignedUrls.add(s3FileResponse.getPresignedUrl());
        }

        noticePhotoRepository.saveAll(newPhotoList);

        log.debug("공지사항 사진 일괄 업로드 완료 - 총 {}개", newPhotoList.size());
        return presignedUrls;
    }
}