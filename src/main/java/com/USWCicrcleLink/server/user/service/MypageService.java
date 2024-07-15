package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.ClubMembers;
import com.USWCicrcleLink.server.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.MyAplictResponse;
import com.USWCicrcleLink.server.user.dto.MyClubResponse;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MypageService {
    private final UserRepository userRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final ProfileRepository profileRepository;
    private final AplictRepository aplictRepository;
    private final ClubRepository clubRepository;

    //UUID를 통해 유저 아이디 조회
    public User getUserByUUID(UUID uuid) {
        User user = userRepository.findByUserUUID(uuid);
        if (user == null) {
            throw new IllegalArgumentException("UUID에 해당하는 유저를 찾을 수 없습니다. " + uuid);
        }
        return user;
    }

    //UUID를 통해 소속된 동아리 조회
    public List<MyClubResponse> getMyClubsByUUID(UUID uuid) {
        User user = getUserByUUID(uuid);
        List<ClubMembers> clubMembers = getClubMembersByUserId(user.getUserId());
        return getMyClubs(clubMembers);
    }

    //유저아이디를 통해 클럽멤버 조회
    private List<ClubMembers> getClubMembersByUserId(Long userId) {
        List<ClubMembers> clubMembers = clubMembersRepository.findByUserUserId(userId);
        if (clubMembers.isEmpty()) {
            throw new IllegalArgumentException("해당 유저가 소속된 클럽을 찾을 수 없습니다.: " + userId);
        }
        return clubMembers;
    }

    //클럽멤버를 통해 클럽아이디 조회
    private List<MyClubResponse> getMyClubs(List<ClubMembers> clubMembers) {
        return clubMembers.stream()
                .map(ClubMembers::getClub)
                .map(this::myClubResponse)
                .collect(Collectors.toList());
    }

    // UUID를 통해 지원한 동아리 조회
    public List<MyAplictResponse> getAplictClubByUUID(UUID uuid) {
        User user = getUserByUUID(uuid);
        Profile profile = getProfileByUserId(user.getUserId());

        List<Aplict> aplicts = getAplictsByProfileId(profile.getId());

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
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 프로필을 찾을 수 없습니다.: " + userId));
    }

    //프로필아이디를 통해 어플릭트 아이디 조회
    private List<Aplict> getAplictsByProfileId(Long profileId) {
        List<Aplict> aplicts = aplictRepository.findByProfileId(profileId);
        if (aplicts.isEmpty()) {
            throw new IllegalArgumentException("해당 프로필의 어플리케이션을 찾을 수 없습니다.: " + profileId);
        }
        return aplicts;
    }

    // 어플리케이션 ID를 통해 클럽 조회
    private Club getClubByAplictId(Long aplictId) {
        Aplict aplict = aplictRepository.findById(aplictId)
                .orElseThrow(() -> new IllegalArgumentException("해당 어플리케이션을 찾을 수 없습니다.: " + aplictId));
        return clubRepository.findByClubId(aplict.getClub().getClubId());
    }

    private MyAplictResponse myAplictResponse(Club club, AplictStatus aplictStatus){
        return MyAplictResponse.builder()
                .clubId(club.getClubId())
                .clubName(club.getClubName())
                .clubInsta(club.getClubInsta())
                .katalkID(club.getKatalkID())
                .leaderName(club.getLeaderName())
                .aplictStatus(aplictStatus)
                .mainPhotoPath(club.getMainPhotoPath()).build();
    }
    private MyClubResponse myClubResponse(Club club){
        return MyClubResponse.builder()
                .clubId(club.getClubId())
                .clubName(club.getClubName())
                .clubInsta(club.getClubInsta())
                .katalkID(club.getKatalkID())
                .leaderName(club.getLeaderName())
                .mainPhotoPath(club.getMainPhotoPath()).build();
    }
}
