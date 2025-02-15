package com.USWCicrcleLink.server.club.club.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClubListResponse {
    private UUID clubUUID;
    private String clubName;
    private String mainPhoto;
    private String departmentName;
    private List<String> clubHashtags;

    public ClubListResponse(Club club, String mainPhotoUrl, List<String> clubHashtags) {
        this.clubName = club.getClubName();
        this.mainPhoto = mainPhotoUrl;
        this.departmentName = club.getDepartment().name();
        this.clubUUID = club.getClubUUID();
        this.clubHashtags = clubHashtags;
    }
}

