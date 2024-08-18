//package com.USWCicrcleLink.server.user.service;
//
//import com.USWCicrcleLink.server.aplict.domain.Aplict;
//import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
//import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
//import com.USWCicrcleLink.server.club.club.domain.Club;
//import com.USWCicrcleLink.server.club.club.domain.ClubMembers;
//import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
//import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
//import com.USWCicrcleLink.server.global.exception.ExceptionType;
//import com.USWCicrcleLink.server.global.exception.errortype.AplictException;
//import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
//import com.USWCicrcleLink.server.global.exception.errortype.ClubMemberException;
//import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
//import com.USWCicrcleLink.server.global.security.util.CustomUserDetails;
//import com.USWCicrcleLink.server.profile.domain.Profile;
//import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
//import com.USWCicrcleLink.server.user.domain.User;
//import com.USWCicrcleLink.server.user.dto.MyAplictResponse;
//import com.USWCicrcleLink.server.user.dto.MyClubResponse;
//import com.USWCicrcleLink.server.user.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Transactional
//public class MypageService {
//    private final UserRepository userRepository;
//    private final ClubMembersRepository clubMembersRepository;
//    private final ProfileRepository profileRepository;
//    private final AplictRepository aplictRepository;
//    private final ClubRepository clubRepository;
//
//    //어세스토큰에서 유저정보 가져오기
//    private User getUserByAuth() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//        return userDetails.user();
//    }
//
//    //클럽멤버를 통해 클럽아이디 조회
//    private List<MyClubResponse> getMyClubs(List<ClubMembers> clubMembers) {
//        return clubMembers.stream()
//                .map(ClubMembers::getClub)
//                .map(this::myClubResponse)
//                .collect(Collectors.toList());
//    }
//
//    //프로필을 통해 클럽 멤버 조회
//    private List<ClubMembers>getClubMembersByProfileId(Long profileId){
//        List<ClubMembers> clubMembers = clubMembersRepository.findByProfileProfileId(profileId);
//        if(clubMembers.isEmpty()){
//            throw new ClubMemberException(ExceptionType.CLUB_MEMBER_NOT_EXISTS);
//        }
//        return clubMembers;
//    }
//
//    //UUID를 통해 소속된 동아리 조회
//    public List<MyClubResponse> getMyClubByUUID(){
//        User user = getUserByAuth();
//        Profile profile = getProfileByUserId((user.getUserId()));
//        List<ClubMembers> clubMembers = getClubMembersByProfileId(profile.getProfileId());
//        log.debug("소속 동아리 조회 완료");
//        return getMyClubs(clubMembers);
//    }
//
//    // UUID를 통해 지원한 동아리 조회
//    public List<MyAplictResponse> getAplictClubByUUID() {
//        User user = getUserByAuth(); // 현재 인증된 사용자 가져오기
//        Profile profile = getProfileByUserId(user.getUserId()); // 사용자의 프로필 정보 가져오기
//
//        List<Aplict> aplicts = getAplictsByProfileId(profile.getProfileId()); // 프로필 ID를 통해 지원 정보 가져오기
//        log.info("지원 동아리 조회 완료");
//
//        return aplicts.stream()
//                .map(aplict -> {
//                    // Club을 안전하게 조회, 존재하지 않으면 예외 처리
//                    Club club = clubRepository.findById(aplict.getClub().getClubId())
//                            .orElseThrow(() -> new RuntimeException("해당 클럽을 찾을 수 없습니다."));
//
//                    AplictStatus aplictStatus = aplict.getAplictStatus(); // 지원 상태 가져오기
//                    return myAplictResponse(club, aplictStatus); // 응답 객체 생성
//                })
//                .collect(Collectors.toList());
//    }
//
//
//    //유저아이디를 통해 프로필아이디 조회
//    private Profile getProfileByUserId(Long userId) {
//        return profileRepository.findByUserUserId(userId)
//                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));
//    }
//
//    //프로필아이디를 통해 어플릭트 아이디 조회
//    private List<Aplict> getAplictsByProfileId(Long profileId) {
//        List<Aplict> aplicts = aplictRepository.findByProfileProfileId(profileId);
//        if (aplicts.isEmpty()) {
//            throw new AplictException(ExceptionType.APLICT_NOT_EXISTS);
//        }
//        return aplicts;
//    }
//
//    // 어플리케이션 ID를 통해 클럽 조회
//    private Optional<Club> getClubByAplictId(Long aplictId) {
//        Aplict aplict = aplictRepository.findById(aplictId)
//                .orElseThrow(() -> new AplictException(ExceptionType.APLICT_NOT_EXISTS));
//        return clubRepository.findById(aplict.getClub().getClubId());
//    }
//
//    private MyAplictResponse myAplictResponse(Club club, AplictStatus aplictStatus){
//        return MyAplictResponse.builder()
//                .clubId(club.getClubId())
//                .clubName(club.getClubName())
//                .clubInsta(club.getClubInsta())
//                .leaderHp(club.getLeaderHp())
//                .leaderName(club.getLeaderName())
//                .aplictStatus(aplictStatus)
//                .mainPhotoPath(club.getMainPhotoPath()).build();
//    }
//    private MyClubResponse myClubResponse(Club club){
//        return MyClubResponse.builder()
//                .clubId(club.getClubId())
//                .clubName(club.getClubName())
//                .clubInsta(club.getClubInsta())
//                .leaderHp(club.getLeaderHp())
//                .leaderName(club.getLeaderName())
//                .mainPhotoPath(club.getMainPhotoPath()).build();
//    }
//}
