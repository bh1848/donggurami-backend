package com.USWCicrcleLink.server.global.security.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.JwtException;
import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final LeaderRepository leaderRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final ProfileRepository profileRepository;

    // UUID로 사용자 로드
    @Override
    public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {
        return loadUserByUuidAndRole(uuid, null);
    }

    // UUID + Role로 사용자 로드
    public UserDetails loadUserByUuidAndRole(String uuid, Role role) throws UsernameNotFoundException {
        UUID userUuid;
        try {
            userUuid = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new JwtException(ExceptionType.INVALID_UUID_FORMAT);
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
                        .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));
                List<UUID> clubUUIDs = getUserClubUUIDs(profile.getProfileId());
                return new CustomUserDetails(user, clubUUIDs);
            }
        }

        if (role == null || role == Role.LEADER) {
            Leader leader = leaderRepository.findByLeaderUUID(userUuid).orElse(null);
            if (leader != null) {
                UUID clubUUID = leaderRepository.findClubUUIDByLeaderUUID(leader.getLeaderUUID())
                        .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
                return new CustomLeaderDetails(leader, clubUUID);
            }
        }

        throw new UserException(ExceptionType.USER_NOT_EXISTS);
    }

    // account + Role로 사용자 로드
    public UserDetails loadUserByAccountAndRole(String account, Role role) throws UsernameNotFoundException {
        if (role == null || role == Role.ADMIN) {
            Admin admin = adminRepository.findByAdminAccount(account).orElse(null);
            if (admin != null) {
                return new CustomAdminDetails(admin);
            }
        }

        if (role == null || role == Role.USER) {
            User user = userRepository.findByUserAccount(account).orElse(null);
            if (user != null) {
                Profile profile = profileRepository.findByUser_UserUUID(user.getUserUUID())
                        .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));
                List<UUID> clubUUIDs = getUserClubUUIDs(profile.getProfileId());
                return new CustomUserDetails(user, clubUUIDs);
            }
        }

        if (role == null || role == Role.LEADER) {
            Leader leader = leaderRepository.findByLeaderAccount(account).orElse(null);
            if (leader != null) {
                UUID clubUUID = leaderRepository.findClubUUIDByLeaderUUID(leader.getLeaderUUID())
                        .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
                return new CustomLeaderDetails(leader, clubUUID);
            }
        }

        throw new UserException(ExceptionType.USER_NOT_EXISTS);
    }

    private List<UUID> getUserClubUUIDs(Long profileId) {
        return clubMembersRepository.findClubUUIDsByProfileId(profileId);
    }
}
