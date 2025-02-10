package com.USWCicrcleLink.server.clubLeader.dto.club;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ClubInfoRequest {

    // 필수 입력값
    @NotEmpty(message = "회장 이름은 필수 입력 값입니다.")
    private String leaderName;

    @NotEmpty(message = "회장 전화번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^\\d{11}$|^$", message = "전화번호는 하이픈 없이 11자리여야 합니다.")
    private String leaderHp;

    // 선택 입력값
    @Pattern(
            regexp = "^(https?://)?(www\\.)?instagram\\.com/.+$|^$",
            message = "유효한 인스타그램 링크를 입력해주세요."
    )
    private String clubInsta;

    private String clubRoomNumber;

    @Size(max = 2, message = "해시태그는 2개까지 입력 가능합니다.")
    private List<String> clubHashtag;

    @Size(max = 3, message = "카테고리는 3개까지 입력 가능합니다.")
    private List<String> clubCategoryName;
}
