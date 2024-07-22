package com.USWCicrcleLink.server.club.dto;

import com.USWCicrcleLink.server.club.domain.ClubIntro;
import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClubIntroResponse {
    private Long clubIntroId;
    private Long clubId;
    private String introContent;
    private String introPhotoPath;
    private String additionalPhotoPath1;
    private String additionalPhotoPath2;
    private String googleFormUrl;
    private RecruitmentStatus recruitmentStatus;

    public ClubIntroResponse(ClubIntro clubIntro, RecruitmentStatus recruitmentStatus) {
        this.clubIntroId = clubIntro.getClubIntroId();
        this.clubId = clubIntro.getClub().getClubId();
        this.introContent = clubIntro.getClubIntro();
        this.introPhotoPath = clubIntro.getClubIntroPhotoPath();
        this.additionalPhotoPath1 = clubIntro.getAdditionalPhotoPath1();
        this.additionalPhotoPath2 = clubIntro.getAdditionalPhotoPath2();
        this.googleFormUrl = clubIntro.getGoogleFormUrl();
        this.recruitmentStatus = recruitmentStatus;
    }
}