package com.USWCicrcleLink.server.profile.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import com.USWCicrcleLink.server.profile.dto.DuplicationProfileRequest;
import com.USWCicrcleLink.server.profile.dto.ProfileRequest;
import com.USWCicrcleLink.server.profile.dto.ProfileResponse;
import com.USWCicrcleLink.server.profile.service.ProfileService;
import com.USWCicrcleLink.server.user.service.PasswordService;
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
    private final PasswordService passwordService;

    @PatchMapping("/change")
    public ApiResponse<ProfileResponse> updateProfile(@RequestBody @Validated(ValidationSequence.class) ProfileRequest profileRequest) {
        ProfileResponse profileResponse = profileService.updateProfile(profileRequest);
        return new ApiResponse<>("프로필 수정 성공", profileResponse);
    }

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMyProfile(){
        ProfileResponse profileResponse = profileService.getMyProfile();
        return new ApiResponse<>("프로필 조회 성공", profileResponse);
    }

    // 기존 회원가입시 프로필 중복 확인 및 비밀번호 유효성 검사
    @GetMapping("/duplication-check")
    public ResponseEntity<ApiResponse<String>> checkProfileDuplicated(@RequestBody @Validated(ValidationSequence.class) DuplicationProfileRequest request){
        // 비밀번호 유효성 확인
        passwordService.validatePassword(request.getPassword(), request.getConfirmPassword());
        // 프로필 중복 확인
        profileService.checkProfileDuplicated(request);
        ApiResponse<String> response = new ApiResponse<>("사용 가능한 프로필 입니다.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
