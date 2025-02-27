package com.USWCicrcleLink.server.clubLeader.dto.club;

import com.USWCicrcleLink.server.global.validation.Sanitize;
import com.USWCicrcleLink.server.global.validation.ValidClubRoomNumber;
import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClubInfoRequest {

    @NotBlank(message = "회장 이름은 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 2, max = 30, message = "회장 이름은 2~30자 이내여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "회장 이름은 영어 또는 한글만 입력 가능합니다.", groups = ValidationGroups.PatternGroup.class)
    @Sanitize
    private String leaderName;

    @NotBlank(message = "전화번호는 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 11, max = 11, message = "전화번호는 11자여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^01[0-9]{9}$", message = "올바른 전화번호를 입력하세요.", groups = ValidationGroups.PatternGroup.class)
    @Sanitize
    private String leaderHp;

    @Pattern(
            regexp = "^(https?://)?(www\\.)?instagram\\.com/.+$|^$",
            message = "유효한 인스타그램 링크를 입력해주세요.",
            groups = ValidationGroups.PatternGroup.class)
    @Sanitize
    private String clubInsta;

    @NotBlank(message = "동아리방 호수는 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 1, max = 4, message = "동아리방 호수는 1~4자 이내여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[0-9A-Za-z]{1,4}$", message = "동아리 방 호수는 1~4자의 숫자 또는 영문이어야 합니다.", groups = ValidationGroups.PatternGroup.class)
    @ValidClubRoomNumber
    private String clubRoomNumber;

    @Size(max = 2, message = "해시태그는 2개까지 입력 가능합니다.")
    @Sanitize
    private List<String> clubHashtag;

    @Size(max = 3, message = "카테고리는 3개까지 입력 가능합니다.")
    @Sanitize
    private List<String> clubCategoryName;
}
