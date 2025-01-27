package com.USWCicrcleLink.server.clubLeader.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Data
public class ClubIntroRequest {

    @Size(max = 3000, message = "소개글은 최대 3000자까지 입력 가능합니다.")
    private String clubIntro;

    private String googleFormUrl;

    private List<Integer> orders;

    private List<Integer> deletedOrders;
}
