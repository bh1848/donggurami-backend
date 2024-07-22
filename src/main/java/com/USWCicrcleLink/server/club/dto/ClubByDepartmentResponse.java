package com.USWCicrcleLink.server.club.dto;

import com.USWCicrcleLink.server.club.domain.Club;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
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

