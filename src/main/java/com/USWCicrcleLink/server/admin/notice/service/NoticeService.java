package com.USWCicrcleLink.server.admin.notice.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import com.USWCicrcleLink.server.admin.notice.domain.NoticePhoto;
import com.USWCicrcleLink.server.admin.notice.dto.*;
import com.USWCicrcleLink.server.admin.notice.repository.NoticePhotoRepository;
import com.USWCicrcleLink.server.admin.notice.repository.NoticeRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.NoticeException;
import com.USWCicrcleLink.server.global.security.util.CustomAdminDetails;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.global.util.s3File.dto.S3FileResponse;
import com.USWCicrcleLink.server.global.util.validator.InputValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private static final int FILE_LIMIT = 5; // 최대 업로드 가능한 파일 수
    private static final String S3_NOTICE_PHOTO_DIR = "noticePhoto/"; // 공지사항 사진 경로
    private final NoticeRepository noticeRepository;
    private final NoticePhotoRepository noticePhotoRepository;
    private final S3FileUploadService s3FileUploadService;

    // 공지사항 목록 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<NoticeListResponse> getNotices(Pageable pageable) {
        try {
            return noticeRepository.findAllNotices(pageable);
        } catch (Exception e){
            throw new NoticeException(ExceptionType.NOTICE_CHECKING_ERROR);
        }
    }

    // 공지사항 세부 정보 조회
    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeById(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        List<String> noticePhotoUrls = noticePhotoRepository.findByNotice(notice).stream()
                .sorted(Comparator.comparingInt(NoticePhoto::getOrder))
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getNoticePhotoS3Key()))
                .collect(Collectors.toList());

        return NoticeDetailResponse.from(notice, noticePhotoUrls);
    }

    // 공지사항 생성
    public NoticeDetailResponse createNotice(NoticeCreationRequest request, List<MultipartFile> noticePhotos) {
        Admin admin = getAuthenticatedAdmin();

        // Notice 빌드 및 저장
        Notice notice = Notice.builder()
                .noticeTitle(InputValidator.sanitizeContent(request.getNoticeTitle()))
                .noticeContent(InputValidator.sanitizeContent(request.getNoticeContent()))
                .noticeCreatedAt(LocalDateTime.now())
                .admin(admin)
                .build();
        Notice savedNotice = noticeRepository.save(notice);

        // 사진 순서와 파일 개수 검증 추가
        validatePhotoOrdersAndPhotos(request.getPhotoOrders(), noticePhotos);

        // 사진 처리
        List<String> presignedUrls = handleNoticePhotos(savedNotice, noticePhotos, request.getPhotoOrders());

        log.debug("공지사항 생성 완료 - ID: {}, 첨부된 사진 수: {}", savedNotice.getNoticeId(), noticePhotos == null ? 0 : noticePhotos.size());

        return NoticeDetailResponse.from(savedNotice, presignedUrls);
    }

    // 공지사항 수정
    public NoticeDetailResponse updateNotice(Long noticeId, NoticeUpdateRequest request, List<MultipartFile> noticePhotos) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        // 제목과 내용 업데이트
        notice.updateTitle(InputValidator.sanitizeContent(request.getNoticeTitle()));
        notice.updateContent(InputValidator.sanitizeContent(request.getNoticeContent()));

        // 기존 사진 삭제
        deleteExistingPhotos(notice);

        // 사진 순서와 파일 개수 검증 추가
        validatePhotoOrdersAndPhotos(request.getPhotoOrders(), noticePhotos);

        // 새로운 사진 처리
        List<String> presignedUrls = handleNoticePhotos(notice, noticePhotos, request.getPhotoOrders());
        log.debug("공지사항 수정 완료 - ID: {}, 첨부된 사진 수: {}", notice.getNoticeId(), noticePhotos == null ? 0 : noticePhotos.size());

        return NoticeDetailResponse.from(notice, presignedUrls);
    }

    // 공지사항 삭제
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        // 기존 사진 삭제
        deleteExistingPhotos(notice);

        // 공지사항 삭제
        noticeRepository.delete(notice);
        log.debug("공지사항 삭제 완료 - ID: {}", notice.getNoticeId());
    }

    // 인증된 관리자 정보 가져오기
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
                throw new NoticeException(ExceptionType.PHOTO_ORDER_MISMATCH);
            }

            // 사진 개수 제한 확인
            if (noticePhotos.size() > FILE_LIMIT) {
                throw new NoticeException(ExceptionType.UP_TO_5_PHOTOS_CAN_BE_UPLOADED);
            }
        }
    }

    // 기존 사진 삭제
    private void deleteExistingPhotos(Notice notice) {
        List<NoticePhoto> existingPhotos = noticePhotoRepository.findByNotice(notice);
        existingPhotos.forEach(photo -> {
            s3FileUploadService.deleteFile(photo.getNoticePhotoS3Key());
            noticePhotoRepository.delete(photo);
            log.debug("삭제된 사진 ID: {}, 파일명: {}", photo.getNoticePhotoId(), photo.getNoticePhotoS3Key());
        });
    }

    // 사진 처리 로직
    private List<String> handleNoticePhotos(Notice notice, List<MultipartFile> noticePhotos, List<Integer> photoOrders) {
        List<String> presignedUrls = new ArrayList<>();

        if (noticePhotos != null && !noticePhotos.isEmpty()) {
            // 사진이 존재할 경우에만 처리
            for (int i = 0; i < noticePhotos.size(); i++) {
                MultipartFile noticePhoto = noticePhotos.get(i);
                int order = photoOrders.get(i);

                if (noticePhoto == null || noticePhoto.isEmpty()) {
                    continue;
                }

                // 새로운 NoticePhoto 생성 및 처리
                NoticePhoto newPhoto = new NoticePhoto();
                newPhoto.setNotice(notice);
                newPhoto.setOrder(order);

                // S3 파일 업로드 및 presigned URL 생성
                S3FileResponse s3FileResponse = updateNoticePhotoAndS3File(noticePhoto, newPhoto, order);
                presignedUrls.add(s3FileResponse.getPresignedUrl());
            }
        }

        return presignedUrls;
    }

    // S3 파일 업로드 및 NoticePhoto 업데이트
    private S3FileResponse updateNoticePhotoAndS3File(MultipartFile noticePhoto, NoticePhoto newPhoto, int order) {
        S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(noticePhoto, S3_NOTICE_PHOTO_DIR);
        newPhoto.updateNoticePhoto(noticePhoto.getOriginalFilename(), s3FileResponse.getS3FileName(), order);
        noticePhotoRepository.save(newPhoto);
        log.debug("새로운 사진 정보 저장 및 업데이트 완료: {}", s3FileResponse.getS3FileName());
        return s3FileResponse;
    }
}