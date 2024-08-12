package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubDetailResponse {
    private Long clubId;
    private String clubName;
    private String leaderName;
    private String phone;
    private String instagram;
    private String mainPhotoPath;
    private String introPhotoPath;
    private String clubIntro;

    public ClubDetailResponse(Club club, ClubIntro clubIntro) {
        this.clubId = club.getClubId();
        this.clubName = club.getClubName();
        this.leaderName = club.getLeaderName();
        this.instagram = club.getClubInsta();
        this.mainPhotoPath = club.getMainPhotoPath();
        this.introPhotoPath = clubIntro != null ? clubIntro.getClubIntroPhotoPath() : null;
        this.clubIntro = clubIntro != null ? clubIntro.getClubIntro() : null;
    }
}