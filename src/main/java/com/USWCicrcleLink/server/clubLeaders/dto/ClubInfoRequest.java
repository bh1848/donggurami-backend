package com.USWCicrcleLink.server.clubLeaders.dto;

import com.USWCicrcleLink.server.clubLeaders.domain.Department;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubInfoRequest {

    //2MB
    private MultipartFile mainPhoto;

    @NotBlank(message = "동아리 이름은 필수 입력 값입니다.")
    private String clubName;

    @NotBlank(message = "회장 이름은 필수 입력 값입니다.")
    private String leaderName;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "소속 분과는 필수 입력 값입니다.")
    private Department department;

    private String chatRoomURL;

    private String katalkID;

    private String clubInsta;
}
