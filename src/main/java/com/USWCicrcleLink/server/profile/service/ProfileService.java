package com.USWCicrcleLink.server.profile.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.util.CustomUserDetails;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.profile.dto.ProfileRequest;
import com.USWCicrcleLink.server.profile.dto.ProfileResponse;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;

    //프로필 업데이트
    public ProfileResponse updateProfile(ProfileRequest profileRequest) {

        validateProfileRequest(profileRequest);

        Profile profile = getProfileByAuth();

        profile.updateProfile(profileRequest);

        Profile updatedProfile = profileRepository.save(profile);

        if (updatedProfile == null) {
            log.error("프로필 업데이트 실패");
            throw new ProfileException(ExceptionType.PROFILE_UPDATE_FAIL);
        }

        log.info("프로필 수정 완료");
        return new ProfileResponse(profile);
    }

    private void validateProfileRequest(ProfileRequest profileRequest) {
        if (profileRequest.getUserName() == null || profileRequest.getUserName().trim().isEmpty() ||
                profileRequest.getStudentNumber() == null || profileRequest.getStudentNumber().trim().isEmpty() ||
                profileRequest.getUserHp() == null || profileRequest.getUserHp().trim().isEmpty() ||
                profileRequest.getMajor() == null || profileRequest.getMajor().trim().isEmpty()) {

            throw new ProfileException(ExceptionType.PROFILE_NOT_INPUT);
        }
    }


    private Profile getProfileByAuth() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.user();

        return profileRepository.findByUserUserId(user.getUserId())
                .orElseThrow(()-> {log.error("존재하지 않는 프로필");
                    throw new ProfileException(ExceptionType.PROFILE_NOT_EXISTS);});
    }

    //프로필 조회
    public ProfileResponse getMyProfile(){
        Profile profile = getProfileByAuth();
        return new ProfileResponse(profile);
    }
}
