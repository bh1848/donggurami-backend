package com.USWCicrcleLink.server.club.clubIntro.dto;

import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubIntroResponse {
    private String introContent;
    private String introPhotoPath;
    private String additionalPhotoPath1;
    private String additionalPhotoPath2;
    private RecruitmentStatus recruitmentStatus;

    public ClubIntroResponse(ClubIntro clubIntro) {
        this.introContent = clubIntro.getClubIntro();
        this.introPhotoPath = clubIntro.getClubIntroPhotoPath();
        this.additionalPhotoPath1 = clubIntro.getAdditionalPhotoPath1();
        this.additionalPhotoPath2 = clubIntro.getAdditionalPhotoPath2();
        this.recruitmentStatus = clubIntro.getRecruitmentStatus();
    }
}