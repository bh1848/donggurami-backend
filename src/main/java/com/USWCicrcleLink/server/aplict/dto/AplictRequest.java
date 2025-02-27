package com.USWCicrcleLink.server.aplict.dto;

import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AplictRequest {
    @NotBlank(message = "지원서 구글폼은 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    private String aplictGoogleFormUrl;
}
