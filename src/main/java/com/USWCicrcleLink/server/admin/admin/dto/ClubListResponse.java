package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMembers;
import com.USWCicrcleLink.server.club.club.domain.Department;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubListResponse {
    private Long clubId;
    private Department department;
    private String clubName;
    private String leaderName;
    private long numberOfClubMembers;

    public ClubListResponse(Club club, long memberCount, long leaderCount) {
        this.clubId = club.getClubId();
        this.department = club.getDepartment();
        this.clubName = club.getClubName();
        this.leaderName = club.getLeaderName();
        this.numberOfClubMembers = memberCount + leaderCount;
    }
}