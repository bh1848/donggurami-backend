package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.global.validation.Sanitize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminClubCategoryCreationRequest {

    @NotBlank(message = "카테고리 이름은 필수 입력 값입니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "카테고리에는 공백 또는 특수문자를 포함할 수 없습니다.")
    @Sanitize
    private String clubCategoryName;
}
