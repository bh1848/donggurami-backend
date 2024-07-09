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

    // token 대신 uuid(식별 용도)
    private UUID leaderUUID;

    private String clubIntro;

    private MultipartFile introPhoto;

    private MultipartFile additionalPhoto1;

    private MultipartFile additionalPhoto2;

}
