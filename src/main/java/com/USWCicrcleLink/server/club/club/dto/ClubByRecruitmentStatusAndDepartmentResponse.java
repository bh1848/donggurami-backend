package com.USWCicrcleLink.server.club.club.dto;

import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubByRecruitmentStatusAndDepartmentResponse {
    private String clubName;
    private String mainPhoto;
    private String departmentName;

    public ClubByRecruitmentStatusAndDepartmentResponse(ClubIntro clubIntro, String mainPhotoUrl) {
        this.clubName = clubIntro.getClub().getClubName();
        this.mainPhoto = mainPhotoUrl;
        this.departmentName = clubIntro.getClub().getDepartment().name();

    }
}

