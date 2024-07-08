package com.USWCicrcleLink.server.club.dto;

import com.USWCicrcleLink.server.club.domain.Club;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClubByDepartmentResponse {
    private Long clubId;
    private String clubName;
    private String mainPhotoPath;

    public ClubByDepartmentResponse(Club club) {
        this.clubId = club.getClubId();
        this.clubName = club.getClubName();
        this.mainPhotoPath = club.getMainPhotoPath();
    }
}