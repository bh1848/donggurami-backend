package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMainPhoto;
import com.USWCicrcleLink.server.club.club.domain.ClubMembers;
import com.USWCicrcleLink.server.club.club.repository.ClubMainPhotoRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.AplictException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubMemberException;
import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
import com.USWCicrcleLink.server.global.security.util.CustomUserDetails;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.MyAplictResponse;
import com.USWCicrcleLink.server.user.dto.MyClubResponse;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MypageService {
    private final ClubMembersRepository clubMembersRepository;
    private final ProfileRepository profileRepository;
    private final AplictRepository aplictRepository;
    private final ClubRepository clubRepository;
    private final S3FileUploadService s3FileUploadService;
    private final ClubMainPhotoRepository clubMainPhotoRepository;

    //어세스토큰에서 유저정보 가져오기
    private User getUserByAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.user();
    }

    //클럽멤버를 통해 클럽아이디 조회
    private List<MyClubResponse> getMyClubs(List<ClubMembers> clubMembers) {
        return clubMembers.stream()
                .map(ClubMembers::getClub)
                .map(this::myClubResponse)
                .collect(Collectors.toList());
    }

    //프로필을 통해 클럽 멤버 조회
    private List<ClubMembers>getClubMembersByProfileId(Long profileId){
        List<ClubMembers> clubMembers = clubMembersRepository.findByProfileProfileId(profileId);
        if(clubMembers.isEmpty()){
            throw new ClubMemberException(ExceptionType.CLUB_MEMBER_NOT_EXISTS);
        }
        return clubMembers;
    }

    //UUID를 통해 소속된 동아리 조회
    public List<MyClubResponse> getMyClubByUUID(){
        User user = getUserByAuth();
        Profile profile = getProfileByUserId((user.getUserId()));
        List<ClubMembers> clubMembers = getClubMembersByProfileId(profile.getProfileId());
        log.debug("소속 동아리 조회 완료");
        return getMyClubs(clubMembers);
    }

    // UUID를 통해 지원한 동아리 조회
    public List<MyAplictResponse> getAplictClubByUUID() {
        User user = getUserByAuth();
        Profile profile = getProfileByUserId(user.getUserId());

        List<Aplict> aplicts = getAplictsByProfileId(profile.getProfileId());
        log.debug("지원 동아리 조회 완료");

        return aplicts.stream()
                .map(aplict -> {
                    Club club = getClubByAplictId(aplict.getId());
                    AplictStatus aplictStatus = aplict.getAplictStatus(); // 어플릭트의 상태 가져오기
                    return myAplictResponse(club, aplictStatus);
                })
                .collect(Collectors.toList());
    }

    //유저아이디를 통해 프로필아이디 조회
    private Profile getProfileByUserId(Long userId) {
        return profileRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));
    }

    //프로필아이디를 통해 어플릭트 아이디 조회
    private List<Aplict> getAplictsByProfileId(Long profileId) {
        List<Aplict> aplicts = aplictRepository.findByProfileProfileId(profileId);
        if (aplicts.isEmpty()) {
            throw new AplictException(ExceptionType.APLICT_NOT_EXISTS);
        }
        return aplicts;
    }

    // 어플리케이션 ID를 통해 클럽 조회
    private Club getClubByAplictId(Long aplictId) {
        Aplict aplict = aplictRepository.findById(aplictId)
                .orElseThrow(() -> new AplictException(ExceptionType.APLICT_NOT_EXISTS));
        return clubRepository.findById(aplict.getClub().getClubId()).orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));
    }

    //사진 조회 url
    private String getClubMainPhotoUrl(Club club) {
        return Optional.ofNullable(clubMainPhotoRepository.findByClub_ClubId(club.getClubId()))
                .map(clubMainPhoto -> s3FileUploadService.generatePresignedGetUrl(clubMainPhoto.getClubMainPhotoS3Key()))
                .orElse(null);
    }

    private MyAplictResponse myAplictResponse(Club club, AplictStatus aplictStatus){

        String mainPhotoUrl = getClubMainPhotoUrl(club);

        MyAplictResponse myAplictResponse = new MyAplictResponse(
                club.getClubId(),
                mainPhotoUrl,
                club.getClubName(),
                club.getLeaderName(),
                club.getLeaderHp(),
                club.getClubInsta(),
                aplictStatus
        );
        return myAplictResponse;
    }
    private MyClubResponse myClubResponse(Club club){

        String mainPhotoUrl = getClubMainPhotoUrl(club);

        MyClubResponse myClubResponse = new MyClubResponse(
                club.getClubId(),
                mainPhotoUrl,
                club.getClubName(),
                club.getLeaderName(),
                club.getLeaderHp(),
                club.getClubInsta()
        );
        return  myClubResponse;
    }
}
