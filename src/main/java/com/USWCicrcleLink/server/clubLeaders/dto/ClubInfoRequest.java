package com.USWCicrcleLink.server.clubLeaders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubInfoRequest {

    // token 대신 uuid(식별 용도)
    private UUID leaderUUID;

    //2MB
    private MultipartFile mainPhoto;

    private String chatRoomURL;

    private String katalkID;

    private String clubInsta;
}
