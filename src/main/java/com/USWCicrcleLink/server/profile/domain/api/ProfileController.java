package com.USWCicrcleLink.server.profile.domain.api;

import com.USWCicrcleLink.server.profile.domain.service.ProfileService;
import com.USWCicrcleLink.server.profile.domain.dto.ProfileRequest;
import com.USWCicrcleLink.server.profile.domain.dto.ProfileResponse;
import com.USWCicrcleLink.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PatchMapping("/update-profile")
    public ResponseEntity<ProfileResponse> updateProfile(@RequestHeader("userId") User user, @RequestBody ProfileRequest profileRequest) {
        ProfileResponse profileResponse = profileService.updateProfile(user, profileRequest);
        return ResponseEntity.ok(profileResponse);
    }


}
