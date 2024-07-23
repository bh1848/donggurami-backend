package com.USWCicrcleLink.server.clubLeader.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class ClubIntroRequest {

    private String clubIntro;

    private MultipartFile introPhoto;

    private MultipartFile additionalPhoto1;

    private MultipartFile additionalPhoto2;

}
