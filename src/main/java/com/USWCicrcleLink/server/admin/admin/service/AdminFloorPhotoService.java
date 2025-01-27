package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.club.club.domain.FloorPhoto;
import com.USWCicrcleLink.server.club.club.domain.FloorPhotoEnum;
import com.USWCicrcleLink.server.club.club.repository.FloorPhotoRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.global.util.s3File.dto.S3FileResponse;
import com.USWCicrcleLink.server.admin.admin.dto.FloorPhotoCreationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminFloorPhotoService {

    private static final String S3_FLOOR_PHOTO_DIR = "floorPhoto/"; // 층별 사진 저장 디렉토리
    private final FloorPhotoRepository floorPhotoRepository;
    private final S3FileUploadService s3FileUploadService;

    // 동아리 위치 정보 수정(웹) - 층별 사진 업로드
    public FloorPhotoCreationResponse uploadPhoto(FloorPhotoEnum floor, MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new BaseException(ExceptionType.PHOTO_FILE_IS_EMPTY);
        }

        // 기존 사진 조회
        FloorPhoto existingPhoto = floorPhotoRepository.findByFloor(floor).orElse(null);

        if (existingPhoto != null) {
            // 기존 사진 삭제 (S3 및 DB)
            s3FileUploadService.deleteFile(existingPhoto.getFloorPhotoPhotoS3key());
            floorPhotoRepository.delete(existingPhoto);
            log.debug("기존 해당 층 사진 삭제 완료: Floor={}, S3Key={}", floor, existingPhoto.getFloorPhotoPhotoS3key());
        }

        // 새로운 사진 업로드
        S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(photo, S3_FLOOR_PHOTO_DIR);

        // 새로운 FloorPhoto 엔티티 생성 및 저장
        FloorPhoto newPhoto = FloorPhoto.builder()
                .floor(floor)
                .floorPhotoPhotoName(photo.getOriginalFilename())
                .floorPhotoPhotoS3key(s3FileResponse.getS3FileName())
                .build();
        floorPhotoRepository.save(newPhoto);

        log.debug("해당 층 사진 업로드 완료: Floor={}, S3Key={}", floor, s3FileResponse.getS3FileName());

        // 응답 DTO 생성 및 반환
        return new FloorPhotoCreationResponse(floor, s3FileResponse.getPresignedUrl());
    }

    // 동아리 위치 정보 수정(웹) - 특정 층 사진 조회
    @Transactional(readOnly = true)
    public FloorPhotoCreationResponse getPhotoByFloor(FloorPhotoEnum floor) {
        FloorPhoto floorPhoto = floorPhotoRepository.findByFloor(floor)
                .orElseThrow(() -> new BaseException(ExceptionType.PHOTO_NOT_FOUND));

        // S3 presigned URL 생성
        String presignedUrl = s3FileUploadService.generatePresignedGetUrl(floorPhoto.getFloorPhotoPhotoS3key());
        return new FloorPhotoCreationResponse(floor, presignedUrl);
    }

    // 동아리 위치 정보 수정(웹) - 특정 층 사진 삭제
    public void deletePhotoByFloor(FloorPhotoEnum floor) {
        FloorPhoto floorPhoto = floorPhotoRepository.findByFloor(floor)
                .orElseThrow(() -> new BaseException(ExceptionType.PHOTO_NOT_FOUND));

        // S3 파일 삭제
        s3FileUploadService.deleteFile(floorPhoto.getFloorPhotoPhotoS3key());

        // 데이터베이스에서 삭제
        floorPhotoRepository.delete(floorPhoto);

        log.debug("해당 층 사진 삭제 완료: Floor={}, S3Key={}", floor, floorPhoto.getFloorPhotoPhotoS3key());
    }
}
