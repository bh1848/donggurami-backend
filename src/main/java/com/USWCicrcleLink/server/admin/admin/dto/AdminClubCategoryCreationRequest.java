package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminClubCategoryCreationRequest {

    @NotBlank(message = "카테고리 이름은 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    @Size(max = 20, message = "카테고리는 1~20자 이내여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "카테고리에는 공백 또는 특수문자를 포함할 수 없습니다.", groups = ValidationGroups.PatternGroup.class)
    private String clubCategoryName;
}
