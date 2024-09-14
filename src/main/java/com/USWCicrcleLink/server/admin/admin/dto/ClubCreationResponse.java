package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.Department;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubCreationResponse {
    private Long clubId;
    private String leaderAccount;
    private String clubName;
    private Department department;

    public ClubCreationResponse(Club club, Leader leader) {
        this.clubId = club.getClubId();
        this.leaderAccount = leader.getLeaderAccount();
        this.clubName = club.getClubName();
        this.department = club.getDepartment();
    }
}
