package com.USWCicrcleLink.server.global.security.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.util.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.util.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.util.CustomUserDetails;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final LeaderRepository leaderRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final ProfileRepository profileRepository;

    // 주어진 uuid로 사용자 세부 정보 로드
    @Override
    public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {
        return loadUserByUuidAndRole(uuid, null);
    }

    // 주어진 uuid와 role로 사용자 세부 정보 로드
    public UserDetails loadUserByUuidAndRole(String uuid, Role role) throws UsernameNotFoundException {
        UUID userUuid;
        try {
            userUuid = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("유효하지 않은 UUID 형식입니다: " + uuid, e);
        }

        if (role == null || role == Role.ADMIN) {
            Admin admin = adminRepository.findByAdminUUID(userUuid).orElse(null);
            if (admin != null) {
                return new CustomAdminDetails(admin);
            }
        }

        if (role == null || role == Role.USER) {
            User user = userRepository.findByUserUUID(userUuid).orElse(null);
            if (user != null) {
                Profile profile = profileRepository.findByUser_UserUUID(userUuid)
                        .orElseThrow(() -> new UsernameNotFoundException("프로필을 찾을 수 없습니다: " + userUuid));
                List<Long> clubIds = getUserClubIds(profile);
                return new CustomUserDetails(user, clubIds);
            }
        }

        if (role == null || role == Role.LEADER) {
            Leader leader = leaderRepository.findByLeaderUUID(userUuid).orElse(null);
            if (leader != null) {
                List<Long> clubIds = List.of(leader.getClub().getClubId());
                return new CustomLeaderDetails(leader, clubIds);
            }
        }

        throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + uuid);
    }

    // 프로필을 통해 사용자 clubId 조회
    private List<Long> getUserClubIds(Profile profile) {
        return clubMembersRepository.findByProfileProfileId(profile.getProfileId())
                .stream()
                .map(clubMember -> clubMember.getClub().getClubId())
                .collect(Collectors.toList());
    }
}