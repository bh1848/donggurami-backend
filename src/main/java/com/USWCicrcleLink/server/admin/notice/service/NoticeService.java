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
import com.USWCicrcleLink.server.global.util.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private final FileUploadService fileUploadService;

    @Value("${file.noticePhoto-dir}")
    private String noticePhotoDir;

    // 공지사항 전체 리스트 조회(웹)
    @Transactional(readOnly = true)
    public List<NoticeListResponse> getAllNotices() {
        return noticeRepository.findAll().stream()
                .map(NoticeListResponse::from)
                .collect(Collectors.toList());
    }

    // 공지사항 리스트 조회(페이징)(웹)
    @Transactional(readOnly = true)
    public PagedModel<NoticeListResponse> getNotices(Pageable pageable, PagedResourcesAssembler<Notice> pagedResourcesAssembler) {
        Page<Notice> noticePage = noticeRepository.findAll(pageable);
        return pagedResourcesAssembler.toModel(noticePage, noticeListResponseAssembler);
    }

    // 공지사항 내용 조회(웹)
    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeById(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        // 공지사항에 연결된 사진 목록 조회 후 정렬
        List<String> noticePhotoPaths = noticePhotoRepository.findByNotice(notice).stream()
                .sorted(Comparator.comparingInt(NoticePhoto::getOrder)) // 순서대로 정렬
                .map(NoticePhoto::getNoticePhotoPath)
                .collect(Collectors.toList());

        return NoticeDetailResponse.from(notice, noticePhotoPaths);
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

        // 공지사항에 첨부된 사진 저장
        List<NoticePhoto> savedNoticePhotos = saveNoticePhotos(noticePhotos, savedNotice, request.getPhotoOrders());
        noticePhotoRepository.saveAll(savedNoticePhotos);

        return NoticeDetailResponse.from(savedNotice, getPhotoPaths(savedNoticePhotos));
    }

    // 공지사항 수정(웹)
    public NoticeDetailResponse updateNotice(Long noticeId, NoticeUpdateRequest request, MultipartFile[] noticePhotos) throws IOException {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        // 공지사항 제목이 수정된 경우에만 업데이트
        if (request.getNoticeTitle() != null) {
            notice.updateTitle(request.getNoticeTitle());
        }

        // 공지사항 내용이 수정된 경우에만 업데이트
        if (request.getNoticeContent() != null) {
            notice.updateContent(request.getNoticeContent());
        }

        // 공지사항 사진이 수정된 경우 처리
        if (noticePhotos != null && noticePhotos.length > 0) {
            updateNoticePhotos(notice, request.getPhotoIds(), request.getPhotoOrders(), noticePhotos);
        }

        Notice updatedNotice = noticeRepository.save(notice);
        List<String> photoPaths = getNoticePhotoPaths(updatedNotice);

        return NoticeDetailResponse.from(updatedNotice, photoPaths);
    }

    // 공지사항 사진 업데이트
    private void updateNoticePhotos(Notice notice, List<Long> existingPhotoIds, List<Integer> photoOrders, MultipartFile[] newPhotos) throws IOException {
        List<NoticePhoto> existingPhotos = noticePhotoRepository.findByNotice(notice);

        // 1. 기존 사진 중 유지할 사진 선택
        List<NoticePhoto> photosToKeep = existingPhotos.stream()
                .filter(photo -> existingPhotoIds.contains(photo.getNoticePhotoId()))
                .collect(Collectors.toList());

        // 2. 삭제할 사진 처리
        List<NoticePhoto> photosToRemove = existingPhotos.stream()
                .filter(photo -> !existingPhotoIds.contains(photo.getNoticePhotoId()))
                .collect(Collectors.toList());
        photosToRemove.forEach(photo -> deletePhotoFile(photo.getNoticePhotoPath()));
        noticePhotoRepository.deleteAll(photosToRemove);

        // 3. 새로 추가된 사진 저장 및 순서 설정
        List<NoticePhoto> newSavedPhotos = new ArrayList<>();
        if (newPhotos != null && newPhotos.length > 0) {
            int startOrderIndex = photosToKeep.size(); // 기존 사진 이후의 순서
            for (int i = 0; i < newPhotos.length; i++) {
                MultipartFile photo = newPhotos[i];
                String newPhotoHash = calculateFileHash(photo);

                // 동일한 사진이 이미 존재하는지 확인
                boolean isDuplicate;
                isDuplicate = photosToKeep.stream()
                        .anyMatch(existingPhoto -> {
                            try {
                                return newPhotoHash.equals(calculateFileHash(existingPhoto.getNoticePhotoPath()));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                if (!isDuplicate) {
                    String photoPath = fileUploadService.saveFile(photo, null, noticePhotoDir);
                    NoticePhoto noticePhoto = NoticePhoto.builder()
                            .noticePhotoPath(photoPath)
                            .notice(notice)
                            .order(photoOrders.get(startOrderIndex + i))  // 새로운 사진의 순서 설정
                            .build();
                    newSavedPhotos.add(noticePhoto);
                } else {
                    log.info("같은 사진 파일이 이미 존재하여 추가되지 않았습니다: {}", photo.getOriginalFilename());
                }
            }
            noticePhotoRepository.saveAll(newSavedPhotos);
        }

        // 4. 기존 사진 순서 업데이트
        if (photoOrders != null && !photoOrders.isEmpty()) {
            for (int i = 0; i < photosToKeep.size(); i++) {
                photosToKeep.get(i).setOrder(photoOrders.get(i)); // 순서 업데이트
            }
        }

        // 5. 전체 사진 리스트 업데이트 및 저장
        photosToKeep.addAll(newSavedPhotos);
        noticePhotoRepository.saveAll(photosToKeep);
    }

    // 파일의 해시값을 계산하는 메서드
    private String calculateFileHash(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return DigestUtils.md5Hex(inputStream);
        }
    }

    // 파일 경로에서 해시값을 계산하는 메서드
    private String calculateFileHash(String filePath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(Path.of(filePath))) {
            return DigestUtils.md5Hex(inputStream);
        }
    }

    // 공지사항 사진 저장 및 순서 설정
    private List<NoticePhoto> saveNoticePhotos(MultipartFile[] photos, Notice notice, List<Integer> orders) throws IOException {
        List<NoticePhoto> savedPhotos = new ArrayList<>();

        // 사진과 순서 정보가 모두 제공되어야 함
        if (photos == null || orders == null || photos.length == 0 || orders.isEmpty()) {
            log.error("사진 또는 순서 정보가 제공되지 않았습니다.");
            throw new NoticeException(ExceptionType.INVALID_PHOTO_DATA);
        }

        // 사진 개수와 순서 정보 개수가 일치해야 함
        if (photos.length != orders.size()) {
            log.error("사진의 개수와 순서 정보의 개수가 일치하지 않습니다.");
            throw new NoticeException(ExceptionType.PHOTO_ORDER_MISMATCH);
        }

        // 사진을 저장하고 순서 정보 설정
        for (int i = 0; i < photos.length; i++) {
            MultipartFile photo = photos[i];
            String photoPath = fileUploadService.saveFile(photo, null, noticePhotoDir);
            NoticePhoto noticePhoto = NoticePhoto.builder()
                    .noticePhotoPath(photoPath)
                    .notice(notice)
                    .order(orders.get(i))  // 사진 순서 반영
                    .build();
            savedPhotos.add(noticePhoto);
        }

        return savedPhotos;
    }

    // 기존 사진 파일 삭제
    private void deletePhotoFile(String photoPath) {
        if (photoPath == null) {
            log.error("삭제하려는 사진 파일 경로가 null입니다.");
            return;
        }
        try {
            fileUploadService.deleteFile(Path.of(photoPath));
        } catch (IOException e) {
            log.error("사진 파일 삭제 중 오류가 발생했습니다. 경로: {}", photoPath, e);
        }
    }

    // 공지사항에 연결된 사진 파일 경로 목록 조회
    private List<String> getNoticePhotoPaths(Notice notice) {
        return noticePhotoRepository.findByNotice(notice).stream()
                .sorted(Comparator.comparingInt(NoticePhoto::getOrder)) // 순서대로 정렬
                .map(NoticePhoto::getNoticePhotoPath)
                .collect(Collectors.toList());
    }

    // 공지사항의 사진 객체 리스트에서 파일 경로 목록 조회
    private List<String> getPhotoPaths(List<NoticePhoto> photos) {
        return photos.stream()
                .sorted(Comparator.comparingInt(NoticePhoto::getOrder)) // 순서대로 정렬
                .map(NoticePhoto::getNoticePhotoPath)
                .collect(Collectors.toList());
    }

    // 공지사항 삭제(웹)
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        List<NoticePhoto> photos = noticePhotoRepository.findByNotice(notice);

        // 기존 사진 파일 삭제
        photos.forEach(photo -> deletePhotoFile(photo.getNoticePhotoPath()));

        // 공지사항에 연결된 사진 정보 삭제
        noticePhotoRepository.deleteByNotice(notice);

        // 공지사항 삭제
        noticeRepository.delete(notice);
    }
}
