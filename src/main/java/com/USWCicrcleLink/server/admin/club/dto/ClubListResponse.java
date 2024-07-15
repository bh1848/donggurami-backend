package com.USWCicrcleLink.server.admin.club.dto;

import com.USWCicrcleLink.server.club.domain.Department;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ClubListResponse{
    private Long clubId;
    private Department department;
    private String clubName;
    private String leaderName;
    private int numberOfClubMembers;
}