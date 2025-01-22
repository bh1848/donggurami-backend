package com.USWCicrcleLink.server.clubLeader.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ClubNonMemberUpdateRequest {

    @NotNull(message = "이름은 필수 입력 값입니다")
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "이름은 한글만 입력 가능합니다")
    private String userName;

    @NotNull(message = "학번은 필수 입력 값입니다")
    @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력 가능합니다")
    private String studentNumber;

    @NotNull(message = "전화번호는 필수 입력 값입니다")
    @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력 가능합니다")
    private String userHp;

    @NotNull(message = "학과는 필수 입력 값입니다")
    private String major;
}
