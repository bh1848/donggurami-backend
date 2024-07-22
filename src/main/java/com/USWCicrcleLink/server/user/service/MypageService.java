package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.ClubMembers;
import com.USWCicrcleLink.server.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.MyPageResponse;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    // UUID를 통해 지원한 동아리 조회
    public List<MyPageResponse> getAplictClubByUUID(UUID uuid) {
        User user = getUserByUUID(uuid);
        Profile profile = getProfileByUserId(user.getUserId());

        List<Aplict> aplicts = getAplictsByProfileId(profile.getId());

        return aplicts.stream()
                .map(aplict -> getClubByAplictId(aplict.getId()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    //UUID를 통해 유저 아이디 조회
    public User getUserByUUID(UUID uuid) {
        User user = userRepository.findByUserUUID(uuid);
        if (user == null) {
            throw new RuntimeException("UUID에 해당하는 유저를 찾을 수 없습니다. " + uuid);
        }
        return user;
    }

    //클럽멤버를 통해 클럽아이디 조회
    private List<MyPageResponse> getMyClubs(List<ClubMembers> clubMembers) {
        return clubMembers.stream()
                .map(ClubMembers::getClub)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    //유저아이디를 통해 프로필아이디 조회
    private Profile getProfileByUserId(Long userId) {
        return profileRepository.findByUserUserId(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저의 프로필을 찾을 수 없습니다.: " + userId));
    }

    //프로필아이디를 통해 어플릭트 아이디 조회
    private List<Aplict> getAplictsByProfileId(Long profileId) {
        List<Aplict> aplicts = aplictRepository.findByProfileId(profileId);
        if (aplicts.isEmpty()) {
            throw new RuntimeException("해당 프로필의 어플리케이션을 찾을 수 없습니다.: " + profileId);
        }
        return aplicts;
    }

    // 어플리케이션 ID를 통해 클럽 조회
    private Club getClubByAplictId(Long aplictId) {
        Aplict aplict = aplictRepository.findById(aplictId)
                .orElseThrow(() -> new RuntimeException("해당 어플리케이션을 찾을 수 없습니다.: " + aplictId));
        return clubRepository.findByClubId(aplict.getClub().getClubId());
    }

    private MyPageResponse convertToResponse(Club club){
        return MyPageResponse.builder()
                .clubId(club.getClubId())
                .clubName(club.getClubName())
                .clubInsta(club.getClubInsta())
                .katalkID(club.getKatalkID())
                .leaderName(club.getLeaderName())
                .mainPhotoPath(club.getMainPhotoPath()).build();
    }
}
