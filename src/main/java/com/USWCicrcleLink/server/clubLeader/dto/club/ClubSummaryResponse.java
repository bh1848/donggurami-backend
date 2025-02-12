package com.USWCicrcleLink.server.clubLeader.dto.club;

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
public class ClubSummaryResponse {

    // club
    private long clubId;
    private String clubName;
    private String leaderName;
    private String leaderHp;
    private String clubInsta;
    private String clubRoomNumber;

    // clubHashtag
    private List<String> clubHashtag;

    // clubIntro
    private String clubIntro;
    private String clubRecruitment;
    private RecruitmentStatus recruitmentStatus;
    private String googleFormUrl;

    // photo
    private String mainPhoto;
    private List<String> introPhotos;

    public ClubSummaryResponse(Club club, List<String> clubHashtag,
                               ClubIntro clubIntro, String mainPhotoUrl, List<String> introPhotoUrls) {
        // club
        this.clubId = club.getClubId();
        this.clubName = club.getClubName();
        this.leaderName = club.getLeaderName();
        this.leaderHp = club.getLeaderHp();
        this.clubInsta = club.getClubInsta();
        this.clubRoomNumber = club.getClubRoomNumber();
        // clubHashtag
        this.clubHashtag = clubHashtag;
        // clubIntro
        this.clubIntro = clubIntro.getClubIntro();
        this.clubRecruitment = clubIntro.getClubRecruitment();
        this.recruitmentStatus = clubIntro.getRecruitmentStatus();
        this.googleFormUrl = clubIntro.getGoogleFormUrl();
        // photo
        this.mainPhoto = mainPhotoUrl;
        this.introPhotos = introPhotoUrls;
    }
}
