package com.USWCicrcleLink.server.clubLeader.dto.clubMembers;

import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ClubMemberProfileRequest {

    @NotNull(message = "대상을 선택해주세요.")
    private UUID uuid;// clubMemberUUID, clubMemberAccountStatusUUID

    @NotBlank(message = "이름 필수 입력 값입니다.")
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "이름은 영어 또는 한글만 입력 가능합니다", groups = ValidationGroups.PatternGroup.class)
    private String userName;

    @NotBlank(message = "학번 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    @Size(max =8, message = "학번은 최대 8자리 입니다",groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력 가능 합니다",groups = ValidationGroups.PatternGroup.class)
    private String studentNumber;

    @NotBlank(message = "전화번호는 필수 입력값 입니다")
    @Size(max =11, message = "전화번호는 11자리까지 입력가능합니다",groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력 가능 합니다", groups = ValidationGroups.PatternGroup.class)
    private String userHp;

    @NotBlank(message = "학과 필수 입력 값입니다.")
    private String major;
}
