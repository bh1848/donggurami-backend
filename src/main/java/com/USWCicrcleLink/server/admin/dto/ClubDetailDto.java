package com.USWCicrcleLink.server.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClubDetailDto {
    private String clubName;
    private String leaderName;
    private String phone;
    private String instagram;
    private String mainPhotoPath;
    private String introPhotoPath;
    private String chatRoomUrl;
    private String introContent;
}
