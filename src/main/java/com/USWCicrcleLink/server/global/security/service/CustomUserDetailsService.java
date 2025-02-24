package com.USWCicrcleLink.server.global.security.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
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

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final LeaderRepository leaderRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final ProfileRepository profileRepository;

    /**
     * 사용자 로그인: User는 Role을 받지 않고 자동 판별, Admin/Leader는 Role을 요청값에서 받음.
     */
    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        return loadUserByAccount(account);
    }

    private UserDetails loadUserByAccount(String account) {
        User user = userRepository.findByUserAccount(account)
                .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));

        return loadUserByUuidAndRole(user.getUserUUID(), Role.USER);
    }

    /**
     * Admin/Leader는 요청된 Role을 기준으로 검색, User는 자동 판별 (UUID 기반)
     */
    public UserDetails loadUserByUuidAndRole(UUID uuid, Role role) throws UsernameNotFoundException {
        if (role == null) {
            throw new UserException(ExceptionType.INVALID_ROLE);
        }

        // Admin 계정인지 확인
        if (role == Role.ADMIN) {
            return adminRepository.findByAdminUUID(uuid)
                    .map(CustomAdminDetails::new)
                    .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
        }

        // Leader 계정인지 확인
        if (role == Role.LEADER) {
            Leader leader = leaderRepository.findByLeaderUUID(uuid)
                    .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));

            UUID clubUUID = leaderRepository.findClubUUIDByLeaderUUID(leader.getLeaderUUID())
                    .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));

            return new CustomLeaderDetails(leader, clubUUID);
        }

        // User 계정인지 확인
        if (role == Role.USER) {
            return loadUserByUuid(uuid);
        }

        throw new UserException(ExceptionType.INVALID_ROLE);
    }

    /**
     * User는 Role 없이 자동 판별하여 로그인 수행 (UUID 기반)
     */
    public UserDetails loadUserByUuid(UUID uuid) throws UsernameNotFoundException {
        // Admin 계정인지 확인
        Admin admin = adminRepository.findByAdminUUID(uuid).orElse(null);
        if (admin != null) {
            return new CustomAdminDetails(admin);
        }

        // Leader 계정인지 확인
        Leader leader = leaderRepository.findByLeaderUUID(uuid).orElse(null);
        if (leader != null) {
            UUID clubUUID = leaderRepository.findClubUUIDByLeaderUUID(leader.getLeaderUUID())
                    .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
            return new CustomLeaderDetails(leader, clubUUID);
        }

        // User 계정인지 확인
        User user = userRepository.findByUserUUID(uuid)
                .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));

        Profile profile = profileRepository.findByUser_UserUUID(user.getUserUUID())
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));

        List<UUID> clubUUIDs = getUserClubUUIDs(profile.getProfileId());
        return new CustomUserDetails(user, clubUUIDs);
    }


    /**
     *  account 기반 조회, 로그인 시에만 사용
     */
    public UserDetails loadUserByAccountAndRole(String account, Role role) throws UsernameNotFoundException {
        if (role == null) {
            throw new UserException(ExceptionType.INVALID_ROLE);
        }

        if (role == Role.ADMIN) {
            return adminRepository.findByAdminAccount(account)
                    .map(CustomAdminDetails::new)
                    .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
        }

        if (role == Role.LEADER) {
            Leader leader = leaderRepository.findByLeaderAccount(account)
                    .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));

            UUID clubUUID = leaderRepository.findClubUUIDByLeaderUUID(leader.getLeaderUUID())
                    .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));

            return new CustomLeaderDetails(leader, clubUUID);
        }

        if (role == Role.USER) {
            return loadUserByAccount(account);
        }

        throw new UserException(ExceptionType.INVALID_ROLE);
    }

    // User가 가입한 Club UUID 목록 가져오기
    private List<UUID> getUserClubUUIDs(Long profileId) {
        return clubMembersRepository.findClubUUIDsByProfileId(profileId);
    }
}
