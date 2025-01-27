package com.USWCicrcleLink.server.profile.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import com.USWCicrcleLink.server.profile.dto.ProfileRequest;
import com.USWCicrcleLink.server.profile.dto.ProfileResponse;
import com.USWCicrcleLink.server.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PatchMapping("/change")
    public ApiResponse<ProfileResponse> updateProfile(@Validated(ValidationSequence.class) @RequestBody ProfileRequest profileRequest) {
        ProfileResponse profileResponse = profileService.updateProfile(profileRequest);
        return new ApiResponse<>("프로필 수정 성공", profileResponse);
    }

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMyProfile(){
        ProfileResponse profileResponse = profileService.getMyProfile();
        return new ApiResponse<>("프로필 조회 성공", profileResponse);
    }

    @GetMapping("/duplication-check")
    public ResponseEntity<ApiResponse<String>> checkProfileDuplicated(@Validated(ValidationSequence.class) @RequestBody ProfileRequest request){
        profileService.checkProfileDuplicated(request);

        ApiResponse<String> response = new ApiResponse<>("사용 가능한 프로필 입니다.",request.getUserName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
