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

    private final NoticeRepository noticeRepository;
    private final NoticeListResponseAssembler noticeListResponseAssembler;
    private final NoticePhotoRepository noticePhotoRepository;
    private final S3FileUploadService s3FileUploadService;

    private static final int FILE_LIMIT = 5; // 업로드 가능한 파일 갯수
    private static final String S3_NOTICE_PHOTO_DIR = "noticePhoto/"; // 공지사항 사진 경로

    // 공지사항 리스트 조회(페이징)(웹)
    @Transactional(readOnly = true)
    public PagedModel<NoticeListResponse> getNotices(Pageable pageable, PagedResourcesAssembler<Notice> pagedResourcesAssembler) {
        Page<Notice> noticePage = noticeRepository.findAll(pageable);
        return pagedResourcesAssembler.toModel(noticePage, noticeListResponseAssembler);
    }

    // 공지사항 세부내용 조회(웹)
    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeById(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        // 공지사항에 연결된 사진 목록 조회 후 S3 URL로 변환
        List<String> noticePhotoUrls = noticePhotoRepository.findByNotice(notice).stream()
                .sorted(Comparator.comparingInt(NoticePhoto::getOrder)) // 순서대로 정렬
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getNoticePhotoS3Key()))
                .toList();

        return NoticeDetailResponse.from(notice, noticePhotoUrls);
    }

    // 공지사항 생성(웹)
    public NoticeDetailResponse createNotice(NoticeCreationRequest request, List<MultipartFile> noticePhotos) {
        // 현재 인증된 관리자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        Admin admin = adminDetails.admin();

        // 입력값 검증 (XSS 공격 방지)
        String sanitizedTitle = InputValidator.sanitizeContent(request.getNoticeTitle());
        String sanitizedContent = InputValidator.sanitizeContent(request.getNoticeContent());

        // 새로운 공지사항 객체 생성 및 저장
        Notice notice = Notice.builder()
                .noticeTitle(sanitizedTitle)
                .noticeContent(sanitizedContent)
                .noticeCreatedAt(LocalDateTime.now())
                .admin(admin)
                .build();
        Notice savedNotice = noticeRepository.save(notice);

        // 각 사진의 presignedUrls
        List<String> presignedUrls = new ArrayList<>();

        // 공지사항에 첨부된 사진 처리
        if (noticePhotos != null && !noticePhotos.isEmpty() && request.getPhotoOrders() != null) {
            if (noticePhotos.size() > FILE_LIMIT) { // 최대 FILE_LIMIT장 업로드
                throw new NoticeException(ExceptionType.MAXIMUM_FILE_LIMIT_EXCEEDED);
            }

            // N번째 사진 1장씩 처리
            for (int i = 0; i < noticePhotos.size(); i++) {
                MultipartFile noticePhoto = noticePhotos.get(i);
                int order = request.getPhotoOrders().get(i);

                if (noticePhoto == null || noticePhoto.isEmpty()) {
                    continue;
                }

                // 새로운 NoticePhoto 객체 생성
                NoticePhoto newPhoto = new NoticePhoto();
                newPhoto.setNotice(savedNotice);
                newPhoto.setOrder(order);

                // 새로운 파일 업로드 및 메타 데이터 업데이트
                S3FileResponse s3FileResponse = updateNoticePhotoAndS3File(noticePhoto, newPhoto, order);

                // 업로드된 사진의 사전 서명된 URL을 리스트에 추가
                presignedUrls.add(s3FileResponse.getPresignedUrl());
            }
        }

        return NoticeDetailResponse.from(savedNotice, presignedUrls);
    }

    // 공지사항 수정(웹)
    public NoticeDetailResponse updateNotice(Long noticeId, NoticeUpdateRequest request, List<MultipartFile> noticePhotos) {
        // 공지사항 존재 여부 확인
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        if (request.getNoticeTitle() == null || request.getNoticeContent() == null) {
            throw new NoticeException(ExceptionType.TITEL_AND_CONENT_REQUIRED);
        }

        // 입력값 검증 (XSS 공격 방지)
        String sanitizedTitle = InputValidator.sanitizeContent(request.getNoticeTitle());
        String sanitizedContent = InputValidator.sanitizeContent(request.getNoticeContent());

        // 제목 및 내용 업데이트 (PUT 방식에서는 필수)
        notice.updateTitle(sanitizedTitle);
        notice.updateContent(sanitizedContent);

        // 기존 사진 유지 및 삭제 처리
        List<NoticePhoto> existingPhotos = noticePhotoRepository.findByNotice(notice);
        List<NoticePhoto> photosToKeep = new ArrayList<>();
        List<NoticePhoto> photosToRemove = new ArrayList<>();

        // 기존 사진 중에서 유지할 사진과 삭제할 사진 구분
        for (NoticePhoto photo : existingPhotos) {
            if (request.getPhotoIds().contains(photo.getNoticePhotoId())) {
                photosToKeep.add(photo);
            } else {
                photosToRemove.add(photo);
            }
        }

        // S3에서 삭제할 사진 제거
        photosToRemove.forEach(photo -> s3FileUploadService.deleteFile(photo.getNoticePhotoS3Key()));
        noticePhotoRepository.deleteAll(photosToRemove);

        // 새로운 사진 추가 및 순서 지정
        List<String> presignedUrls = new ArrayList<>();
        if (noticePhotos != null && !noticePhotos.isEmpty() && request.getPhotoOrders() != null) {
            for (int i = 0; i < noticePhotos.size(); i++) {
                MultipartFile noticePhoto = noticePhotos.get(i);
                int order = request.getPhotoOrders().get(i);

                if (noticePhoto == null || noticePhoto.isEmpty()) {
                    continue;
                }

                NoticePhoto newPhoto = new NoticePhoto();
                newPhoto.setNotice(notice);
                newPhoto.setOrder(order);

                // 파일 업로드 및 NoticePhoto 업데이트
                S3FileResponse s3FileResponse = updateNoticePhotoAndS3File(noticePhoto, newPhoto, order);
                noticePhotoRepository.save(newPhoto);

                // 업로드된 사진의 URL을 리스트에 추가
                presignedUrls.add(s3FileResponse.getPresignedUrl());
            }
        }

        Notice updatedNotice = noticeRepository.save(notice);

        // 최종 반환할 사진 URL 리스트 생성
        List<String> photoUrls = photosToKeep.stream()
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getNoticePhotoS3Key()))
                .collect(Collectors.toList());
        photoUrls.addAll(presignedUrls);

        return NoticeDetailResponse.from(updatedNotice, photoUrls);
    }

    private S3FileResponse updateNoticePhotoAndS3File(MultipartFile noticePhoto, NoticePhoto newPhoto, int order) {
        // 새로운 파일 업로드
        S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(noticePhoto, S3_NOTICE_PHOTO_DIR);

        // s3key 및 photoname 업데이트
        newPhoto.updateNoticePhoto(noticePhoto.getOriginalFilename(), s3FileResponse.getS3FileName(), order);
        noticePhotoRepository.save(newPhoto); // 새로운 객체 저장
        log.debug("새로운 사진 정보 저장 및 업데이트 완료: {}", s3FileResponse.getS3FileName());

        return s3FileResponse;
    }

    // 공지사항 삭제(웹)
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        List<NoticePhoto> photos = noticePhotoRepository.findByNotice(notice);

        // S3에서 기존 사진 파일 삭제
        photos.forEach(photo -> s3FileUploadService.deleteFile(photo.getNoticePhotoS3Key()));

        // 공지사항에 연결된 사진 정보 삭제
        noticePhotoRepository.deleteAll(photos);

        // 공지사항 삭제
        noticeRepository.delete(notice);
    }
}