package com.USWCicrcleLink.server.clubLeader.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubIntroWebResponse {

    private long clubId;
    private String mainPhoto;
    private List<String> introPhotos;
    private String clubName;
    private String leaderName;
    private String leaderHp;
    private String clubInsta;
    private String clubIntro;
    private String clubRecruitment;
    private RecruitmentStatus recruitmentStatus;
    private String googleFormUrl;

    public ClubIntroWebResponse(ClubIntro clubIntro, Club club, String clubRecruitment, String mainPhotoUrl, List<String> introPhotoUrls) {
        this.clubId = club.getClubId();
        this.mainPhoto = mainPhotoUrl;
        this.introPhotos = introPhotoUrls;
        this.clubName = club.getClubName();
        this.leaderName = club.getLeaderName();
        this.leaderHp = club.getLeaderHp();
        this.clubInsta = club.getClubInsta();
        this.clubIntro = clubIntro.getClubIntro();
        this.clubRecruitment = clubRecruitment;
        this.recruitmentStatus = clubIntro.getRecruitmentStatus();
        this.googleFormUrl = clubIntro.getGoogleFormUrl();
    }
}
