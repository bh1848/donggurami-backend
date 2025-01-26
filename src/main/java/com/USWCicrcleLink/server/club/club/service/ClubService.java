package com.USWCicrcleLink.server.club.club.service;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMainPhoto;
import com.USWCicrcleLink.server.club.club.dto.ClubListResponse;
import com.USWCicrcleLink.server.club.club.repository.ClubMainPhotoRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.user.dto.ClubInfoListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMainPhotoRepository clubMainPhotoRepository;
    private final S3FileUploadService s3FileUploadService;

    // 전체 동아리 리스트 조회
    @Transactional(readOnly = true)
    public List<ClubInfoListResponse> getAllClubs() {
        log.debug("전체 동아리 리스트 조회");
        List<Club> clubs = clubRepository.findAll();

        return clubs.stream()
                .map(club -> {
                    // ClubMainPhoto 조회
                    ClubMainPhoto clubMainPhoto = clubMainPhotoRepository.findByClub(club).orElseThrow(() -> new ClubException(ExceptionType.CLUB_MAINPHOTO_NOT_EXISTS));
                    // S3 presigned URL 생성 (기본 URL 또는 null 처리)
                    String mainPhotoUrl = (clubMainPhoto != null)
                            ? s3FileUploadService.generatePresignedGetUrl(clubMainPhoto.getClubMainPhotoS3Key())
                            : null;

                    // DTO 생성
                    return new ClubInfoListResponse(club,mainPhotoUrl);  // 전체 동아리 조회용 DTO로 수정
                })
                .collect(Collectors.toList());
    }


}
