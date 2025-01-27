package com.USWCicrcleLink.server.club.club.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubFilterResponse {
    private Long categoryId;
    private String category;
    private List<ClubResponse> clubs;

    public ClubFilterResponse(ClubCategory clubCategory, List<ClubResponse> clubs){
        this.categoryId = clubCategory.getClubCategoryId();
        this.category = clubCategory.getClubCategory();
        this.clubs = clubs;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ClubResponse {
        private Long clubId;
        private String clubName;
        private String mainPhoto;
        private List<String> clubHashtags;

        public ClubResponse(Club club, String mainPhoto, List<String> clubHashtags){
            this.clubId = club.getClubId();
            this.clubName = club.getClubName();
            this.mainPhoto = mainPhoto;
            this.clubHashtags = clubHashtags;
        }
    }
}
