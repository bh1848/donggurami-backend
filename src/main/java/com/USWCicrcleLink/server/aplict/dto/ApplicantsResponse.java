package com.USWCicrcleLink.server.aplict.dto;

import com.USWCicrcleLink.server.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantsResponse {

    private Long aplictId;

    private String userName;

    private String major;

    private String studentNumber;

    private String userHp;

    public ApplicantsResponse(Long aplictId, Profile profile) {
        this.aplictId = aplictId;
        this.userName = profile.getUserName();
        this.major = profile.getMajor();
        this.studentNumber = profile.getStudentNumber();
        this.userHp = profile.getUserHp();
    }
}
