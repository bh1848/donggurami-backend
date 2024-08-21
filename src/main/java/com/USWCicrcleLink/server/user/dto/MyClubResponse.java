package com.USWCicrcleLink.server.user.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyClubResponse {

    private Long clubId;

    private String mainPhotoPath;

    private String clubName;

    private String leaderName;

    private String leaderHp;

    private String clubInsta;

}
