package com.USWCicrcleLink.server.clubLeaders.dto;

import com.USWCicrcleLink.server.clubLeaders.domain.ClubIntro;
import com.USWCicrcleLink.server.clubLeaders.domain.RecruitmentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ClubIntroResponse {
    private Long clubIntroId;
    private Long clubId;
    private String introContent;
    private String introPhotoPath;
    private String additionalPhotoPath1;
    private String additionalPhotoPath2;
    private String googleFormUrl;
    private RecruitmentStatus recruitmentStatus;
    private LocalDate recruitmentStartDate;
    private LocalDate recruitmentEndDate;

    public ClubIntroResponse(ClubIntro clubIntro, RecruitmentStatus recruitmentStatus) {
        this.clubIntroId = clubIntro.getClubIntroId();
        this.clubId = clubIntro.getClub().getClubId();
        this.introContent = clubIntro.getIntroContent();
        this.introPhotoPath = clubIntro.getIntroPhotoPath();
        this.additionalPhotoPath1 = clubIntro.getAdditionalPhotoPath1();
        this.additionalPhotoPath2 = clubIntro.getAdditionalPhotoPath2();
        this.googleFormUrl = clubIntro.getGoogleFormUrl();
        this.recruitmentStatus = recruitmentStatus;
        this.recruitmentStartDate = clubIntro.getRecruitmentStartDate();
        this.recruitmentEndDate = clubIntro.getRecruitmentEndDate();
    }
}