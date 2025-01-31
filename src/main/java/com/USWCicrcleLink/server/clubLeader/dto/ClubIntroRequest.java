package com.USWCicrcleLink.server.clubLeader.dto;

import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Data
public class ClubIntroRequest {

    @Size(max = 3000, message = "소개글은 최대 3000자까지 입력 가능합니다.")
    private String clubIntro;

    @NotNull(message = "모집 상태를 설정해주세요.")
    private RecruitmentStatus recruitmentStatus;

    @Size(max = 3000, message = "모집글은 최대 3000자까지 입력 가능합니다.")
    private String clubRecruitment;

    private String googleFormUrl;

    private List<Integer> orders;

    private List<Integer> deletedOrders;

}
