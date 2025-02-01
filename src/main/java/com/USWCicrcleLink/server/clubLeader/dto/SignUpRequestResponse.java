package com.USWCicrcleLink.server.clubLeader.dto;

import com.USWCicrcleLink.server.user.domain.ClubMemberTemp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequestResponse {

    private Long clubMemberAccountStatusId;

    private String profileTempName;

    private String profileTempStudentNumber;

    private String profileTempMajor;

    private String profileTempHp;

    public SignUpRequestResponse(Long clubMemberAccountStatusId, ClubMemberTemp clubMemberTemp) {
        this.clubMemberAccountStatusId = clubMemberAccountStatusId;
        this.profileTempName = clubMemberTemp.getProfileTempName();
        this.profileTempStudentNumber = clubMemberTemp.getProfileTempStudentNumber();
        this.profileTempMajor = clubMemberTemp.getProfileTempMajor();
        this.profileTempHp = clubMemberTemp.getProfileTempHp();
    }
}
