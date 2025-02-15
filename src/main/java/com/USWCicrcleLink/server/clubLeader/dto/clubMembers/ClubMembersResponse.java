package com.USWCicrcleLink.server.clubLeader.dto.clubMembers;

import com.USWCicrcleLink.server.profile.domain.MemberType;
import com.USWCicrcleLink.server.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubMembersResponse {

    private UUID clubMemberUUID;

    private String userName;

    private String major;

    private String studentNumber;

    private String userHp;

    private MemberType memberType;

    public ClubMembersResponse(UUID clubMemberUUID, Profile profile) {
        this.clubMemberUUID = clubMemberUUID;
        this.userName = profile.getUserName();
        this.major = profile.getMajor();
        this.studentNumber = profile.getStudentNumber();
        this.userHp = profile.getUserHp();
        this.memberType = profile.getMemberType();
    }
}
