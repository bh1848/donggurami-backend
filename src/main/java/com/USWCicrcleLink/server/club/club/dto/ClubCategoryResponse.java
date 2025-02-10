package com.USWCicrcleLink.server.club.club.dto;

import com.USWCicrcleLink.server.club.club.domain.ClubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubCategoryResponse {
    private Long clubCategoryId;
    private String clubCategoryName;

    public ClubCategoryResponse(ClubCategory clubCategory) {
        this.clubCategoryId = clubCategory.getClubCategoryId();
        this.clubCategoryName = clubCategory.getClubCategoryName();
    }
}
