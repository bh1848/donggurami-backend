package com.USWCicrcleLink.server.club.clubIntro.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMainPhoto;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import lombok.*;

import java.util.List;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubIntroResponse {
    private long clubId;
    private String mainPhoto;
    private List<String> introPhotos;
    private String clubName;
    private String leaderName;
    private String leaderHp;
    private String clubInsta;
    private String clubIntro;
    private RecruitmentStatus recruitmentStatus;
    private List<String> clubHashtag;
    private String ClubRoomNumber;
    private String clubRecruitment;

    public ClubIntroResponse(ClubIntro clubIntro, Club club, String mainPhotoUrl, List<String> introPhotoUrls, List<String> clubHashtag) {
        this.clubId = club.getClubId();
        this.mainPhoto = mainPhotoUrl;  // S3 presigned URL
        this.introPhotos = introPhotoUrls;  // List of S3 presigned URLs
        this.clubName = club.getClubName();
        this.leaderName = club.getLeaderName();
        this.leaderHp = club.getLeaderHp();
        this.clubInsta = club.getClubInsta();
        this.clubIntro = clubIntro.getClubIntro();
        this.recruitmentStatus = clubIntro.getRecruitmentStatus();
        this.clubHashtag = clubHashtag;
        this.ClubRoomNumber = club.getClubRoomNumber();
        this.clubRecruitment = clubIntro.getClubRecruitment();
    }
}
