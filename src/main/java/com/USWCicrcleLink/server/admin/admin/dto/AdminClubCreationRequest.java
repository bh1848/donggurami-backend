package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.club.club.domain.Department;
import com.USWCicrcleLink.server.global.validation.Sanitize;
import com.USWCicrcleLink.server.global.validation.ValidClubRoomNumber;
import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminClubCreationRequest {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Size(min = 5, max = 20, message = "아이디는 5~20자 이내여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문 대소문자 및 숫자만 포함할 수 있으며 공백을 포함할 수 없습니다.")
    @Sanitize
    private String leaderAccount;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자 이내여야 합니다.")
    @Pattern(regexp = "^(?!.*\\s)[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+$",
            message = "비밀번호는 영문 대소문자, 숫자, 특수문자만 포함할 수 있으며 공백을 포함할 수 없습니다.")
    @Sanitize
    private String leaderPw;

    @NotBlank(message = "비밀번호 확인은 필수 입력 값입니다.")
    @Sanitize
    private String leaderPwConfirm;

    @NotBlank(message = "동아리 이름은 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 1, max = 10, message = "동아리 이름은 1~10자 이내여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "동아리 이름은 영어 또는 한글만 입력 가능합니다.", groups = ValidationGroups.PatternGroup.class)
    @Sanitize
    private String clubName;

    @NotNull(message = "학부는 필수 입력 값입니다.")
    @Enumerated(EnumType.STRING)
    private Department department;

    @NotBlank(message = "운영자 비밀번호는 필수 입력 값입니다.")
    @Sanitize
    private String adminPw;

    @NotBlank(message = "동아리방 호수는 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 1, max = 4, message = "동아리방 호수는 1~4자 이내여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[0-9A-Za-z]{1,4}$", message = "동아리 방 호수는 1~4자의 숫자 또는 영문이어야 합니다.", groups = ValidationGroups.PatternGroup.class)
    @ValidClubRoomNumber
    private String clubRoomNumber;
}