package com.USWCicrcleLink.server.club.dto;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.Department;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClubResponse {
    private Long clubId;
    private String clubName;
    private Department department;
    private String mainPhotoPath;
    private String chatRoomUrl;
    private String katalikId;
    private String instaUrl;

    public ClubResponse(Club club) {
        this.clubId = club.getClubId();
        this.clubName = club.getClubName();
        this.department = club.getDepartment();
        this.mainPhotoPath = club.getMainPhotoPath();
        this.chatRoomUrl = club.getChatRoomUrl();
        this.katalikId = club.getKatalkID();
        this.instaUrl = club.getClubInsta();
    }
}