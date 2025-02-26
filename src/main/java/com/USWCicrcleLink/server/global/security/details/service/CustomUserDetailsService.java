package com.USWCicrcleLink.server.global.security.details.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final LeaderRepository leaderRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final ProfileRepository profileRepository;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        return loadUserByAccount(account);
    }

    private UserDetails loadUserByAccount(String account) {
        User user = userRepository.findByUserAccount(account)
                .orElseThrow(() -> new UserException(ExceptionType.USER_AUTHENTICATION_FAILED));
        return loadUserByUuidAndRole(user.getUserUUID(), Role.USER);
    }

    /**
     * Role 기반으로 UUID로 계정을 조회
     * Admin/Leader는 요청받은 Role을 기준으로, User는 자동 판별
     */
    public UserDetails loadUserByUuidAndRole(UUID uuid, Role role) throws UsernameNotFoundException {
        if (role == null) {
            throw new UserException(ExceptionType.INVALID_ROLE);
        }
        switch (role) {
            case ADMIN:
                return adminRepository.findByAdminUUID(uuid)
                        .map(CustomAdminDetails::new)
                        .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
            case LEADER:
                Leader leader = leaderRepository.findByLeaderUUID(uuid)
                        .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
                UUID clubUUID = leaderRepository.findClubUUIDByLeaderUUID(leader.getLeaderUUID())
                        .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
                return new CustomLeaderDetails(leader, clubUUID);
            case USER:
                return loadUserByUuid(uuid);
            default:
                throw new UserException(ExceptionType.INVALID_ROLE);
        }
    }

    /**
     * UUID 기반으로 User를 조회
     * Admin 또는 Leader일 경우에도 해당 계정이 있으면 CustomDetails를 반환
     */
    public UserDetails loadUserByUuid(UUID uuid) throws UsernameNotFoundException {
        // Admin 계정 확인
        Optional<Admin> adminOptional = adminRepository.findByAdminUUID(uuid);
        if (adminOptional.isPresent()) {
            return new CustomAdminDetails(adminOptional.get());
        }
        // Leader 계정 확인
        Optional<Leader> leaderOptional = leaderRepository.findByLeaderUUID(uuid);
        if (leaderOptional.isPresent()) {
            UUID clubUUID = leaderRepository.findClubUUIDByLeaderUUID(leaderOptional.get().getLeaderUUID())
                    .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
            return new CustomLeaderDetails(leaderOptional.get(), clubUUID);
        }
        // User 계정 확인
        User user = userRepository.findByUserUUID(uuid)
                .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
        Profile profile = profileRepository.findByUser_UserUUID(user.getUserUUID())
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));
        List<UUID> clubUUIDs = getUserClubUUIDs(profile.getProfileId());
        return new CustomUserDetails(user, clubUUIDs);
    }

    /**
     * account 기반으로, Role에 따라 계정 조회
     * 로그인 과정
     */
    public UserDetails loadUserByAccountAndRole(String account, Role role) throws UsernameNotFoundException {
        if (role == null) {
            log.error("인증 실패: role이 비어있습니다. account: {}", account);
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }
        switch (role) {
            case ADMIN:
                return adminRepository.findByAdminAccount(account)
                        .map(CustomAdminDetails::new)
                        .orElseThrow(() -> new UserException(ExceptionType.USER_AUTHENTICATION_FAILED));
            case LEADER:
                Leader leader = leaderRepository.findByLeaderAccount(account)
                        .orElseThrow(() -> new UserException(ExceptionType.USER_AUTHENTICATION_FAILED));
                UUID clubUUID = leaderRepository.findClubUUIDByLeaderUUID(leader.getLeaderUUID())
                        .orElseThrow(() -> new UserException(ExceptionType.USER_AUTHENTICATION_FAILED));
                return new CustomLeaderDetails(leader, clubUUID);
            case USER:
                return loadUserByAccount(account);
            default:
                log.error("인증 실패: 유효하지 않은 role {}, account: {}", role, account);
                throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }
    }


    private List<UUID> getUserClubUUIDs(Long profileId) {
        return clubMembersRepository.findClubUUIDsByProfileId(profileId);
    }
}