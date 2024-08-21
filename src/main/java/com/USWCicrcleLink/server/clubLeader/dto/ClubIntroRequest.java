package com.USWCicrcleLink.server.clubLeader.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Data
public class ClubIntroRequest {

    private String clubIntro;

    private String googleFormUrl;

    private List<Integer> orders;

}
