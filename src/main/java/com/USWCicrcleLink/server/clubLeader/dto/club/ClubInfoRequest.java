package com.USWCicrcleLink.server.clubLeader.dto.club;

import com.USWCicrcleLink.server.global.validation.Sanitize;
import com.USWCicrcleLink.server.global.validation.ValidClubRoomNumber;
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

    @NotBlank(message = "회장 이름은 필수 입력 값입니다.")
    @Sanitize
    private String leaderName;

    @NotBlank(message = "회장 전화번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^\\d{11}$|^$", message = "전화번호는 하이픈 없이 11자리여야 합니다.")
    @Sanitize
    private String leaderHp;

    @Pattern(
            regexp = "^(https?://)?(www\\.)?instagram\\.com/.+$|^$",
            message = "유효한 인스타그램 링크를 입력해주세요."
    )
    @Sanitize
    private String clubInsta;

    @NotBlank(message = "동아리방 호수는 필수 입력 값입니다.")
    @ValidClubRoomNumber
    private String clubRoomNumber;

    @Size(max = 2, message = "해시태그는 2개까지 입력 가능합니다.")
    @Sanitize
    private List<String> clubHashtag;

    @Size(max = 3, message = "카테고리는 3개까지 입력 가능합니다.")
    @Sanitize
    private List<String> clubCategoryName;
}
