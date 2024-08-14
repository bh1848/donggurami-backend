package com.USWCicrcleLink.server.club.clubIntro.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubIntroResponse {
    private long clubId;
    private String mainPhotoPath;
    private String introPhotoPath;
    private String clubName;
    private String leaderName;
    private String leaderHp;
    private String clubInsta;
    private String introContent;
    private RecruitmentStatus recruitmentStatus;

    public ClubIntroResponse(ClubIntro clubIntro, Club club) {
        this.clubId = club.getClubId();
        this.mainPhotoPath = club.getMainPhotoPath();
        this.introPhotoPath = clubIntro.getClubIntroPhotoPath();
        this.clubName = club.getClubName();
        this.leaderName = club.getLeaderName();
        this.leaderHp = club.getLeaderHp();
        this.clubInsta = club.getClubInsta();
        this.introContent = clubIntro.getClubIntro();
        this.recruitmentStatus = clubIntro.getRecruitmentStatus();
    }
}