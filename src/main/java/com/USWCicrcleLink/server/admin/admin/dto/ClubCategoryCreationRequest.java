package com.USWCicrcleLink.server.admin.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClubCategoryCreationRequest {

    @NotBlank(message = "카테고리 이름은 필수 입력 값입니다.")
    private String clubCategoryName;

}
