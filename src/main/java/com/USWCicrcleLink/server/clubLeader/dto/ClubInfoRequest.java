package com.USWCicrcleLink.server.clubLeader.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubInfoRequest {

    //2MB
    private MultipartFile mainPhoto;

    private String chatRoomURL;

    private String katalkID;

    private String clubInsta;
}
