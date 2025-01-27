package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.club.club.domain.Department;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminClubListResponse {
    private Long clubId;
    private Department department;
    private String clubName;
    private String leaderName;
    private long numberOfClubMembers;
}