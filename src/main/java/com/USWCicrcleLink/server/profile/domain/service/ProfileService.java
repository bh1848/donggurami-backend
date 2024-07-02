package com.USWCicrcleLink.server.profile.domain.service;

import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.domain.repository.ProfileRepository;
import com.USWCicrcleLink.server.profile.domain.dto.ProfileRequest;
import com.USWCicrcleLink.server.profile.domain.dto.ProfileResponse;
import com.USWCicrcleLink.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileResponse updateProfile(User user, ProfileRequest profileRequest){
        Profile profile = getProfileById(user.getUserId());

        profile.setUserName(profileRequest.getUserName());
        profile.setStudentNumber(profileRequest.getStudentNumber());
        profile.setUserHp(profileRequest.getUserHp());
        profile.setMajor(profileRequest.getMajor());
        profile.setProfileUpdatedAt(LocalDateTime.now());

        profileRepository.save(profile);

        return new ProfileResponse(profile);
    }

    private Profile getProfileById(Long userId) {
        return profileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user id: " + userId));
    }

}
