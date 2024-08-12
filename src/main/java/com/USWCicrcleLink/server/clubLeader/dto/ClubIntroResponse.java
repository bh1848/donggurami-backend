package com.USWCicrcleLink.server.clubLeader.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubIntroResponse {

    private String mainPhoto;

    private String clubIntroPhoto;

    private String clubName;

    private String leaderName;

    private String leaderHp;

    private String clubInsta;

    private String clubIntro;
}