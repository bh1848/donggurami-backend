package com.USWCicrcleLink.server.aplict.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AplictRequest {
    @NotBlank(message = "지원서 구글폼은 필수 입력 값입니다.")
    private String aplictGoogleFormUrl;
}
