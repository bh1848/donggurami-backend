package com.USWCicrcleLink.server.club.club.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubByRecruitmentStatusResponse {
    private Long clubId;
    private String clubName;
    private String mainPhoto;
    private String departmentName;
    private List<String> clubHashtags;


    public ClubByRecruitmentStatusResponse(Club club, ClubIntro clubIntro, String mainPhotoUrl, List<String> clubHashtags) {
        this.clubId = club.getClubId();
        this.clubName = clubIntro.getClub().getClubName();
        this.mainPhoto = mainPhotoUrl;
        this.departmentName = clubIntro.getClub().getDepartment().name();
        this.clubHashtags = clubHashtags;
    }
}

