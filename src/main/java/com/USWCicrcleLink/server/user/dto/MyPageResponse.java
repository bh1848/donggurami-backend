package com.USWCicrcleLink.server.user.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class MyPageResponse {

    private Long clubId;

    private String mainPhotoPath;

    private String clubName;

    private String leaderName;

    private String katalkID;

    private String clubInsta;

}
