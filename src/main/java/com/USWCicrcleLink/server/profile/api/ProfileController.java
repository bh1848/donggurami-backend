package com.USWCicrcleLink.server.profile.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.profile.dto.ProfileRequest;
import com.USWCicrcleLink.server.profile.dto.ProfileResponse;
import com.USWCicrcleLink.server.profile.service.ProfileService;
import com.USWCicrcleLink.server.profile.dto.ProfileRequest;
import com.USWCicrcleLink.server.profile.dto.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PatchMapping("/{uuid}")
    public ApiResponse<ProfileResponse> updateProfile(@PathVariable UUID uuid, @RequestBody ProfileRequest profileRequest) {
        ProfileResponse profileResponse = profileService.updateProfile(uuid, profileRequest);
        return new ApiResponse<>("프로필 수정 성공", profileResponse);
    }

    @GetMapping("/{uuid}")
    public ApiResponse<ProfileResponse> getMyprofile(@PathVariable UUID uuid){
        ProfileResponse profileResponse = profileService.getMyProfile(uuid);
        return new ApiResponse<>("프로필 조회 성공", profileResponse);
    }
}
