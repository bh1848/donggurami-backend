package com.USWCicrcleLink.server.club.club.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubByDepartmentResponse {
    private String clubName;
    private String mainPhotoPath;
    private String departmentName;

    // Club 엔티티를 인수로 받는 생성자
    public ClubByDepartmentResponse(Club club) {
        this.clubName = club.getClubName();
        this.mainPhotoPath = club.getMainPhotoPath();
        this.departmentName = club.getDepartment().name();
    }
}

