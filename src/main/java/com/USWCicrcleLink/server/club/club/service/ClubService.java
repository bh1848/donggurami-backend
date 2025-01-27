
package com.USWCicrcleLink.server.club.club.service;

import com.USWCicrcleLink.server.club.club.domain.*;
import com.USWCicrcleLink.server.club.club.dto.ClubByRecruitmentStatusFilterResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubFilterResponse;
import com.USWCicrcleLink.server.club.club.repository.*;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.user.dto.ClubInfoListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
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

    //카테고리별 전체 동아리 조회
    public List<ClubFilterResponse> getClubsByCategories(List<String> categories) {

        //카테고리 선택을 3개로 제한
        if (categories.size() > 3) {
            throw new BaseException(ExceptionType.INVALID_CATEGORY_COUNT);
        }

        // 각 카테고리에 해당하는 동아리들을 조회
        List<ClubCategory> clubCategories = clubCategoryRepository.findByClubCategoryIn(categories);

        //카테고리 존재 여부 확인
        if(clubCategories.isEmpty()){
            throw  new BaseException(ExceptionType.CATEGORY_NOT_FOUND);
        }

        List<ClubFilterResponse> clubFilterResponseList = new ArrayList<>();

        for (ClubCategory category : clubCategories) {
            // 카테고리에 해당하는 ClubCategoryMapping 조회
            List<ClubCategoryMapping> clubCategoryMappingList = clubCategoryMappingRepository.findByClubCategory(category);

            List<ClubFilterResponse.ClubResponse> clubResponses = new ArrayList<>();

            for (ClubCategoryMapping clubCategoryMapping : clubCategoryMappingList) {
                Club club = clubCategoryMapping.getClub();

                // MainPhoto URL 가져오기
                ClubMainPhoto clubMainPhoto = clubMainPhotoRepository.findByClub(club).orElse(null);
                String mainPhotoUrl = (clubMainPhoto != null)
                        ? s3FileUploadService.generatePresignedGetUrl(clubMainPhoto.getClubMainPhotoS3Key())
                        : null;

                // 해시태그 리스트 가져오기
                List<String> clubHashtags = clubHashtagRepository.findByClub(club).stream()
                        .map(ClubHashtag::getClubHashtag)
                        .collect(Collectors.toList());

                // DTO 생성
                ClubFilterResponse.ClubResponse clubResponse = new ClubFilterResponse.ClubResponse(club, mainPhotoUrl, clubHashtags);
                clubResponses.add(clubResponse);

            }
            clubFilterResponseList.add(new ClubFilterResponse(category, clubResponses));
        }
        return clubFilterResponseList;
    }

    //카테고리별 모집중인 동아리 조회
    public List<ClubByRecruitmentStatusFilterResponse> getClubsByRecruitmentStatusCategories(List<String> categories) {

        // 카테고리 선택을 3개로 제한
        if (categories.size() > 3) {
            throw new BaseException(ExceptionType.INVALID_CATEGORY_COUNT);
        }

        // 각 카테고리에 해당하는 동아리들을 조회
        List<ClubCategory> clubCategories = clubCategoryRepository.findByClubCategoryIn(categories);

        // 카테고리 존재 여부 확인
        if (clubCategories.isEmpty()) {
            throw new BaseException(ExceptionType.CATEGORY_NOT_FOUND);
        }

        // 모집 중인 동아리 조회 및 ID 필터링
        List<ClubIntro> openClubs = clubIntroRepository.findByRecruitmentStatus(RecruitmentStatus.OPEN);
        Set<Long> openClubIds = openClubs.stream()
                .map(clubIntro -> clubIntro.getClub().getClubId())
                .collect(Collectors.toSet());

        List<ClubByRecruitmentStatusFilterResponse> clubFilterResponseList = new ArrayList<>();

        for (ClubCategory category : clubCategories) {
            // 카테고리에 해당하는 ClubCategoryMapping 조회
            List<ClubCategoryMapping> clubCategoryMappingList = clubCategoryMappingRepository.findByClubCategory(category);

            List<ClubFilterResponse.ClubResponse> clubResponses = new ArrayList<>();

            for (ClubCategoryMapping clubCategoryMapping : clubCategoryMappingList) {
                Club club = clubCategoryMapping.getClub();

                // 모집 중인 동아리인지 확인
                if (openClubIds.contains(club.getClubId())) {
                    // MainPhoto URL 가져오기
                    ClubMainPhoto clubMainPhoto = clubMainPhotoRepository.findByClub(club).orElse(null);
                    String mainPhotoUrl = (clubMainPhoto != null)
                            ? s3FileUploadService.generatePresignedGetUrl(clubMainPhoto.getClubMainPhotoS3Key())
                            : null;

                    // 해시태그 리스트 가져오기
                    List<String> clubHashtags = clubHashtagRepository.findByClub(club).stream()
                            .map(ClubHashtag::getClubHashtag)
                            .collect(Collectors.toList());

                    // DTO 생성
                    ClubFilterResponse.ClubResponse clubResponse = new ClubFilterResponse.ClubResponse(club, mainPhotoUrl, clubHashtags);
                    clubResponses.add(clubResponse);
                }
            }
            // 모집 중인 동아리가 있는 경우에만 추가
            if (!clubResponses.isEmpty()) {
                clubFilterResponseList.add(new ClubByRecruitmentStatusFilterResponse(category, clubResponses));
            }
        }
        return clubFilterResponseList;
    }

    // 모바일- 기존 회원가입시 전체 동아리 조회
    @Transactional(readOnly = true)
    public List<ClubInfoListResponse> getAllClubs() {
        log.debug("전체 동아리 리스트 조회");
        List<Club> clubs = clubRepository.findAll();

        return clubs.stream()
                .map(club -> {
                    // ClubMainPhoto 조회
                    ClubMainPhoto clubMainPhoto = clubMainPhotoRepository.findByClub(club).orElse(null);

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
