package com.USWCicrcleLink.server.club.club.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubByRecruitmentStatusResponse {
    private Long clubId;
    private String clubName;
    private String mainPhoto;
    private String departmentName;

    public ClubByRecruitmentStatusResponse(Club club, ClubIntro clubIntro, String mainPhotoUrl) {
        this.clubId = club.getClubId();
        this.clubName = clubIntro.getClub().getClubName();
        this.mainPhoto = mainPhotoUrl;
        this.departmentName = clubIntro.getClub().getDepartment().name();

    }
}

