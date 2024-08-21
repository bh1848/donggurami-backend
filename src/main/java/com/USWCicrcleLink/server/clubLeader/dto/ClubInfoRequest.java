package com.USWCicrcleLink.server.clubLeader.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ClubInfoRequest {

    private String leaderName;

    private String leaderHp;

    private String clubInsta;

}
