package com.USWCicrcleLink.server.club.club.service;

import com.USWCicrcleLink.server.admin.admin.mapper.ClubCategoryMapper;
import com.USWCicrcleLink.server.club.club.domain.*;
import com.USWCicrcleLink.server.club.club.dto.ClubCategoryResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubListByClubCategoryResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubListResponse;
import com.USWCicrcleLink.server.club.club.repository.*;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import com.USWCicrcleLink.server.club.clubIntro.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroPhotoRepository;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubIntroException;
import com.USWCicrcleLink.server.global.s3File.Service.S3FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubService {

    private final ClubCategoryMappingRepository clubCategoryMappingRepository;
    private final ClubCategoryRepository clubCategoryRepository;
    private final ClubMainPhotoRepository clubMainPhotoRepository;
    private final ClubHashtagRepository clubHashtagRepository;
    private final S3FileUploadService s3FileUploadService;
    private final ClubIntroRepository clubIntroRepository;
    private final ClubRepository clubRepository;
    private final ClubIntroPhotoRepository clubIntroPhotoRepository;

    // 전체 동아리 리스트 조회 (모바일)
    @Transactional(readOnly = true)
    public List<ClubListResponse> getAllClubs() {
        log.debug("전체 동아리 리스트 조회");
        return clubRepository.findAll()
                .stream()
                .map(this::mapToClubListResponse)
                .collect(Collectors.toList());
    }

    // 관심 카테고리 필터 적용한 전체 동아리 리스트 조회 (모바일)
    @Transactional(readOnly = true)
    public List<ClubListByClubCategoryResponse> getAllClubsByClubCategories(List<UUID> clubCategoryUUIDs) {
        validateCategoryLimit(clubCategoryUUIDs);

        List<ClubCategory> clubCategories = getValidatedCategories(clubCategoryUUIDs);

        return clubCategories.stream()
                .map(category -> {
                    List<ClubListResponse> clubResponses = clubCategoryMappingRepository.findByClubCategory(category)
                            .stream()
                            .map(mapping -> mapToClubListResponse(mapping.getClub()))
                            .collect(Collectors.toList());

                    return new ClubListByClubCategoryResponse(
                            category.getClubCategoryUUID(),
                            category.getClubCategoryName(),
                            clubResponses
                    );
                })
                .collect(Collectors.toList());
    }

    // 모집 중 동아리 리스트 조회 (모바일)
    @Transactional(readOnly = true)
    public List<ClubListResponse> getOpenClubs() {
        log.debug("모집 중인 동아리 리스트 조회");

        return clubRepository.findOpenClubs()
                .stream()
                .map(this::mapToClubListResponse)
                .collect(Collectors.toList());
    }

    // 관심 카테고리 필터 적용한 모집 중 동아리 리스트 조회 (모바일)
    @Transactional(readOnly = true)
    public List<ClubListByClubCategoryResponse> getOpenClubsByClubCategories(List<UUID> clubCategoryUUIDs) {
        validateCategoryLimit(clubCategoryUUIDs);

        List<ClubCategory> clubCategories = getValidatedCategories(clubCategoryUUIDs);

        // 모집 중인 동아리 목록
        Set<Long> openClubIds = clubIntroRepository.findByRecruitmentStatus(RecruitmentStatus.OPEN)
                .stream()
                .map(clubIntro -> clubIntro.getClub().getClubId())
                .collect(Collectors.toSet());

        return clubCategories.stream()
                .map(category -> {
                    List<ClubListResponse> clubResponses = clubCategoryMappingRepository.findByClubCategory(category)
                            .stream()
                            .map(ClubCategoryMapping::getClub)
                            .filter(club -> openClubIds.contains(club.getClubId()))
                            .map(this::mapToClubListResponse)
                            .collect(Collectors.toList());

                    return new ClubListByClubCategoryResponse(
                            category.getClubCategoryUUID(),
                            category.getClubCategoryName(),
                            clubResponses
                    );
                })
                .collect(Collectors.toList());
    }

    private ClubListResponse mapToClubListResponse(Club club) {
        // 메인 사진 URL 조회
        String mainPhotoUrl = clubMainPhotoRepository.findByClub(club)
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubMainPhotoS3Key()))
                .orElse(null);

        // 해시태그 리스트 조회
        List<String> clubHashtags = clubHashtagRepository.findByClub(club)
                .stream()
                .map(ClubHashtag::getClubHashtag)
                .collect(Collectors.toList());

        return new ClubListResponse(club, mainPhotoUrl, clubHashtags);
    }

    // 카테고리 개수 검증 (최대 3개)
    private void validateCategoryLimit(List<UUID> clubCategoryUUIDs) {
        if (Optional.ofNullable(clubCategoryUUIDs).orElse(Collections.emptyList()).size() > 3) { // ✅ 안전한 처리
            throw new BaseException(ExceptionType.INVALID_CATEGORY_COUNT);
        }
    }

    // 선택한 카테고리 존재하는지 검증
    private List<ClubCategory> getValidatedCategories(List<UUID> clubCategoryUUIDs) {
        List<ClubCategory> clubCategories = clubCategoryRepository.findByClubCategoryUUIDIn(clubCategoryUUIDs);
        if (clubCategories.isEmpty()) {
            throw new BaseException(ExceptionType.CATEGORY_NOT_FOUND);
        }
        return clubCategories;
    }

    // 카테고리 조회
    @Transactional(readOnly = true)
    public List<ClubCategoryResponse> getAllClubCategories() {
        List<ClubCategory> clubCategories = clubCategoryRepository.findAll();
        log.debug("동아리 카테고리 조회 성공 - {}개 카테고리 반환", clubCategories.size());

        return ClubCategoryMapper.toDtoList(clubCategories);
    }


    // 동아리 상세 페이지 조회 (웹, 모바일)
    @Transactional(readOnly = true)
    public ClubIntroResponse getClubIntro(Long clubId) {
        // 동아리 ID로 동아리 조회
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));

        // 동아리 소개 조회
        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(club.getClubId())
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
        List<String> introPhotoUrls = Optional.ofNullable(clubIntroPhotoRepository.findByClubIntro(clubIntro))
                .orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparingInt(ClubIntroPhoto::getOrder))
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubIntroPhotoS3Key()))
                .collect(Collectors.toList());

        // ClubHashtag 조회
        List<String> hashtags = Optional.ofNullable(clubHashtagRepository.findByClub(club))
                .orElse(Collections.emptyList())
                .stream()
                .map(ClubHashtag::getClubHashtag)
                .collect(Collectors.toList());

        // ClubIntroResponse 반환
        return new ClubIntroResponse(clubIntro, club, mainPhotoUrl, introPhotoUrls,hashtags);
    }

}
