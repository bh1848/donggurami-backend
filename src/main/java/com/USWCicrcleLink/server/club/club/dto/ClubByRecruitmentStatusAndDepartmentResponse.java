package com.USWCicrcleLink.server.club.club.dto;

import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubByRecruitmentStatusAndDepartmentResponse {
    private String clubName;
    private String mainPhotoPath;
    private String departmentName;

    public ClubByRecruitmentStatusAndDepartmentResponse(ClubIntro clubIntro) {
        this.clubName = clubIntro.getClub().getClubName();
        this.mainPhotoPath = clubIntro.getClub().getMainPhotoPath();
        this.departmentName = clubIntro.getClub().getDepartment().name();

    }
}

