package com.USWCicrcleLink.server.club.club.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubListResponse {
    private Long clubId;
    private String clubName;
    private String mainPhoto;
    private String departmentName;
    private List<String> clubHashtags;

    // Club 엔티티를 인수로 받는 생성자
    public ClubListResponse(Club club, String mainPhotoUrl, List<String> clubHashtags) {
        this.clubName = club.getClubName();
        this.mainPhoto = mainPhotoUrl;
        this.departmentName = club.getDepartment().name();
        this.clubId = club.getClubId();
        this.clubHashtags = clubHashtags;
    }
}

