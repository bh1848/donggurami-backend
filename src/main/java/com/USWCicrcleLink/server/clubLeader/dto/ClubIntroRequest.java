package com.USWCicrcleLink.server.clubLeader.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubIntroRequest {

    private String clubIntro;

    private MultipartFile introPhoto;

    private MultipartFile additionalPhoto1;

    private MultipartFile additionalPhoto2;

}
