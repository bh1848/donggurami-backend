package com.USWCicrcleLink.server.clubLeader.dto;

import com.USWCicrcleLink.server.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubMembersExcelResponse {

    private String userName;

    private String major;

    private String studentNumber;

    private String userHp;

    public ClubMembersExcelResponse(Profile profile) {
        this.userName = profile.getUserName();
        this.major = profile.getMajor();
        this.studentNumber = profile.getStudentNumber();
        this.userHp = profile.getUserHp();
    }
}
