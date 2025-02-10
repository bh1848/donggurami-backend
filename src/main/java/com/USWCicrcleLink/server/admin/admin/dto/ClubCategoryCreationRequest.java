package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.global.validation.Sanitize;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClubCategoryCreationRequest {

    @NotBlank(message = "카테고리 이름은 필수 입력 값입니다.")
    @Sanitize
    private String clubCategoryName;

}
