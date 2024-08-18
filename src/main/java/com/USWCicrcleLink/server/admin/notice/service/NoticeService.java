package com.USWCicrcleLink.server.admin.notice.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import com.USWCicrcleLink.server.admin.notice.domain.NoticePhoto;
import com.USWCicrcleLink.server.admin.notice.dto.*;
import com.USWCicrcleLink.server.admin.notice.repository.NoticePhotoRepository;
import com.USWCicrcleLink.server.admin.notice.repository.NoticeRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import com.USWCicrcleLink.server.clubLeader.dto.ClubIntroRequest;
import com.USWCicrcleLink.server.clubLeader.dto.UpdateClubIntroResponse;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ClubIntroException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubPhotoException;
import com.USWCicrcleLink.server.global.exception.errortype.FileException;
import com.USWCicrcleLink.server.global.exception.errortype.NoticeException;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.util.CustomAdminDetails;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.global.util.s3File.dto.S3FileResponse;
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

import java.io.IOException;
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

    // 공지사항 생성(웹)
    public NoticeDetailResponse createNotice(NoticeCreationRequest request, MultipartFile[] noticePhotos) throws IOException {
        // 현재 인증된 관리자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        Admin admin = adminDetails.admin();

        // 새로운 공지사항 객체 생성 및 저장
        Notice notice = Notice.builder()
                .noticeTitle(request.getNoticeTitle())
                .noticeContent(request.getNoticeContent())
                .noticeCreatedAt(LocalDateTime.now())
                .admin(admin)
                .build();
        Notice savedNotice = noticeRepository.save(notice);

        // 공지사항에 첨부된 사진 처리
        List<NoticePhoto> savedNoticePhotos = new ArrayList<>();
        if (noticePhotos != null && noticePhotos.length > 0 && request.getPhotoOrders() != null) {
            savedNoticePhotos = saveNoticePhotos(noticePhotos, savedNotice, request.getPhotoOrders());
            noticePhotoRepository.saveAll(savedNoticePhotos);
        }

        // S3 URL 반환
        List<String> photoUrls = getPhotoPaths(savedNoticePhotos);

        return NoticeDetailResponse.from(savedNotice, photoUrls);
    }

    // 공지사항 수정(웹)
    public NoticeDetailResponse updateNotice(Long noticeId, NoticeUpdateRequest request, MultipartFile[] noticePhotos) throws IOException {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        if (request.getNoticeTitle() != null) {
            notice.updateTitle(request.getNoticeTitle());
        }

        if (request.getNoticeContent() != null) {
            notice.updateContent(request.getNoticeContent());
        }

        List<NoticePhoto> existingPhotos = noticePhotoRepository.findByNotice(notice);

        // 1. 기존 사진 유지 및 삭제 처리
        List<NoticePhoto> photosToKeep = new ArrayList<>();
        List<NoticePhoto> photosToRemove = new ArrayList<>();

        for (NoticePhoto photo : existingPhotos) {
            if (request.getPhotoIds() != null && request.getPhotoIds().contains(photo.getNoticePhotoId())) {
                photosToKeep.add(photo);
            } else {
                photosToRemove.add(photo);
            }
        }

        // S3에서 삭제할 사진 제거
        photosToRemove.forEach(photo -> deletePhotoFile(photo.getNoticePhotoPath()));
        noticePhotoRepository.deleteAll(photosToRemove);

        // 2. 새로운 사진 추가 및 순서 업데이트 (사진이 있을 경우에만)
        if (noticePhotos != null && noticePhotos.length > 0 && request.getPhotoOrders() != null) {
            int startOrderIndex = photosToKeep.size(); // 기존 사진 이후의 순서
            for (int i = 0; i < noticePhotos.length; i++) {
                MultipartFile photo = noticePhotos[i];
                S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(photo, S3_NOTICE_PHOTO_DIR);

                NoticePhoto newPhoto = NoticePhoto.builder()
                        .noticePhotoPath(s3FileResponse.getS3FileName()) // S3에 저장된 파일 이름 사용
                        .notice(notice)
                        .order(request.getPhotoOrders().get(startOrderIndex + i)) // 새로운 사진의 순서
                        .build();
                photosToKeep.add(newPhoto);
            }
        }

        // 3. 기존 사진 순서 업데이트
        if (request.getPhotoOrders() != null && !request.getPhotoOrders().isEmpty()) {
            for (int i = 0; i < photosToKeep.size(); i++) {
                photosToKeep.get(i).setOrder(request.getPhotoOrders().get(i)); // 순서 업데이트
            }
        }

        noticePhotoRepository.saveAll(photosToKeep);

        Notice updatedNotice = noticeRepository.save(notice);

        // S3 URL 반환 (사진이 있는 경우에만)
        List<String> photoUrls = getPhotoPaths(photosToKeep);

        return NoticeDetailResponse.from(updatedNotice, photoUrls);
    }

    // 공지사항 내용 조회(웹)
    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeById(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        // 공지사항에 연결된 사진 목록 조회 후 S3 URL로 변환하여 정렬
        List<String> noticePhotoUrls = noticePhotoRepository.findByNotice(notice).stream()
                .sorted(Comparator.comparingInt(NoticePhoto::getOrder)) // 순서대로 정렬
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getNoticePhotoPath()))
                .collect(Collectors.toList());

        return NoticeDetailResponse.from(notice, noticePhotoUrls);
    }

    // 공지사항 삭제(웹)
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        List<NoticePhoto> photos = noticePhotoRepository.findByNotice(notice);

        // S3에서 기존 사진 파일 삭제
        photos.forEach(photo -> deletePhotoFile(photo.getNoticePhotoPath()));

        // 공지사항에 연결된 사진 정보 삭제
        noticePhotoRepository.deleteByNotice(notice);

        // 공지사항 삭제
        noticeRepository.delete(notice);
    }

    // 공지사항 사진 저장 및 순서 설정
    private List<NoticePhoto> saveNoticePhotos(MultipartFile[] photos, Notice notice, List<Integer> orders) throws IOException {
        List<NoticePhoto> savedPhotos = new ArrayList<>();

        if (photos.length > FILE_LIMIT) {
            throw new NoticeException(ExceptionType.MAXIMUM_FILE_LIMIT_EXCEEDED);
        }

        for (int i = 0; i < photos.length; i++) {
            MultipartFile photo = photos[i];

            // S3에 이미지 파일 업로드
            S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(photo, S3_NOTICE_PHOTO_DIR);

            NoticePhoto noticePhoto = NoticePhoto.builder()
                    .noticePhotoPath(s3FileResponse.getS3FileName()) // S3에 저장된 파일 이름 사용
                    .notice(notice)
                    .order(orders.get(i)) // 사진 순서 반영
                    .build();
            savedPhotos.add(noticePhoto);
        }

        return savedPhotos;
    }

    // 공지사항의 사진 객체 리스트에서 S3 URL 목록 조회
    private List<String> getPhotoPaths(List<NoticePhoto> photos) {
        return photos.stream()
                .sorted(Comparator.comparingInt(NoticePhoto::getOrder)) // 순서대로 정렬
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getNoticePhotoPath()))
                .collect(Collectors.toList());
    }

    // 공지사항 사진 삭제
    private void deletePhotoFile(String fileName) {
        if (fileName == null) {
            log.error("삭제하려는 사진 파일 이름이 null입니다.");
            return;
        }
        s3FileUploadService.deleteFile(fileName);
    }
}