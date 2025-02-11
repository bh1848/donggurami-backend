package com.USWCicrcleLink.server.club.clubIntro.dto;

import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
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
    private String clubRoomNumber;
    private String clubRecruitment;
}
