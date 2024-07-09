package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.ClubMembers;
import com.USWCicrcleLink.server.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.MyPageResponse;
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
    private final ClubRepository clubRepository;
    private final ClubMembersRepository clubMembersRepository;

    //UUID를 통해 소속된 동아리 조회
    public List<MyPageResponse> getMyClubsByUUID(UUID uuid) {
        User user = getUserByUUID(uuid);
        List<ClubMembers> clubMembers = getClubMembersByUserId(user.getUserId());
        return getMyClubs(clubMembers);
    }

    //UUID를 통해 유저 아이디 조회
    public User getUserByUUID(UUID uuid) {
        User user = userRepository.findByUserUUID(uuid);
        if (user == null) {
            throw new RuntimeException("UUID에 해당하는 유저를 찾을 수 없습니다. " + uuid);
        }
        return user;
    }

    //유저아이디를 통해 클럽멤버 조회
    public List<ClubMembers> getClubMembersByUserId(Long userId) {
        List<ClubMembers> clubMembers = clubMembersRepository.findByUserUserId(userId);
        if (clubMembers.isEmpty()) {
            throw new RuntimeException("해당 유저가 소속된 클럽을 찾을 수 없습니다.: " + userId);
        }
        return clubMembers;
    }

    //클럽멤버를 통해 클럽아이디 조회
    public List<MyPageResponse> getMyClubs(List<ClubMembers> clubMembers) {
        List<Club> clubs = clubMembers.stream()
                .map(ClubMembers::getClub)
                .collect(Collectors.toList());
        return clubs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private MyPageResponse convertToResponse(Club club){
        return MyPageResponse.builder()
                .clubId(club.getClubId())
                .clubName(club.getClubName())
                .clubInsta(club.getInstaUrl())
                .katalkID(club.getKatalikId())
                .leaderName(club.getLeaderName())
                .mainPhotoPath(club.getMainPhotoPath()).build();
    }
}
