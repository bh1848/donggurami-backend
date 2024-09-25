package com.USWCicrcleLink.server.club.clubIntro.service;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMainPhoto;
import com.USWCicrcleLink.server.club.club.domain.Department;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.club.dto.ClubByDepartmentResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubByRecruitmentStatusAndDepartmentResponse;
import com.USWCicrcleLink.server.club.club.repository.ClubMainPhotoRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import com.USWCicrcleLink.server.club.clubIntro.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroPhotoRepository;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubIntroException;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubIntroService {

    private final ClubIntroRepository clubIntroRepository;
    private final ClubRepository clubRepository;
    private final ClubMainPhotoRepository clubMainPhotoRepository;
    private final ClubIntroPhotoRepository clubIntroPhotoRepository;
    private final S3FileUploadService s3FileUploadService;

    // 분과별 동아리 조회(모바일)
    @Transactional(readOnly = true)
    public List<ClubByDepartmentResponse> getClubsByDepartment(Department department) {
        log.debug("분과별 동아리 조회: {}", department);
        List<Club> clubs = clubRepository.findByDepartment(department);
        if (clubs.isEmpty()) {
            throw new ClubException(ExceptionType.CLUB_NOT_EXISTS);
        }

        return clubs.stream()
                .map(club -> {
                    // ClubMainPhoto 조회
                    ClubMainPhoto clubMainPhoto = clubMainPhotoRepository.findByClub(club).orElse(null);

                    // S3 presigned URL 생성 (기본 URL 또는 null 처리)
                    String mainPhotoUrl = (clubMainPhoto != null)
                            ? s3FileUploadService.generatePresignedGetUrl(clubMainPhoto.getClubMainPhotoS3Key())
                            : null;

                    // DTO 생성
                    return new ClubByDepartmentResponse(club, mainPhotoUrl);
                })
                .collect(Collectors.toList());
    }

    // 모집 상태에 따른 분과별 동아리 조회
    @Transactional(readOnly = true)
    public List<ClubByRecruitmentStatusAndDepartmentResponse> getClubsByRecruitmentStatusAndDepartment(RecruitmentStatus recruitmentStatus, Department department) {
        log.debug("모집 상태 및 분과별 동아리 조회: recruitmentStatus={}, department={}", recruitmentStatus, department);
        List<ClubIntro> clubs = clubIntroRepository.findByRecruitmentStatusAndClub_Department(recruitmentStatus, department);
        if (clubs.isEmpty()) {
            throw new ClubException(ExceptionType.CLUB_NOT_EXISTS);
        }

        return clubs.stream()
                .map(clubIntro -> {

                    Club club = clubIntro.getClub();

                    // ClubMainPhoto 조회
                    ClubMainPhoto clubMainPhoto = clubMainPhotoRepository.findByClub(clubIntro.getClub()).orElse(null);

                    // S3 presigned URL 생성 (기본 URL 또는 null 처리)
                    String mainPhotoUrl = (clubMainPhoto != null)
                            ? s3FileUploadService.generatePresignedGetUrl(clubMainPhoto.getClubMainPhotoS3Key())
                            : null;

                    // DTO 생성
                    return new ClubByRecruitmentStatusAndDepartmentResponse(club, clubIntro, mainPhotoUrl);
                })
                .collect(Collectors.toList());
    }

    // 동아리 상세 페이지 조회(웹, 모바일)
    @Transactional(readOnly = true)
    public ClubIntroResponse getClubIntro(Long clubId) {
        // 동아리 ID로 동아리 조회
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));

        // 동아리 소개 조회
        ClubIntro clubIntro = clubIntroRepository.findByClub(club)
                .orElseThrow(() -> new ClubIntroException(ExceptionType.CLUB_INTRO_NOT_EXISTS));

        // 동아리 메인 사진 조회
        ClubMainPhoto clubMainPhoto = clubMainPhotoRepository.findByClub(club).orElse(null);

        // 동아리 소개 사진 조회
        List<ClubIntroPhoto> clubIntroPhotos = clubIntroPhotoRepository.findByClubIntro(clubIntro);

        // S3에서 메인 사진 URL 생성 (기본 URL 또는 null 처리)
        String mainPhotoUrl = (clubMainPhoto != null)
                ? s3FileUploadService.generatePresignedGetUrl(clubMainPhoto.getClubMainPhotoS3Key())
                : null;

        // S3에서 소개 사진 URL 생성 (소개 사진이 없을 경우 빈 리스트)
        List<String> introPhotoUrls = clubIntroPhotos.isEmpty()
                ? Collections.emptyList()
                : clubIntroPhotos.stream()
                .sorted(Comparator.comparingInt(ClubIntroPhoto::getOrder))
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubIntroPhotoS3Key()))
                .collect(Collectors.toList());

        // ClubIntroResponse 반환
        return new ClubIntroResponse(clubIntro, club, mainPhotoUrl, introPhotoUrls);
    }
}