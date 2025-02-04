package com.USWCicrcleLink.server.clubLeader.dto.clubMembers;

import lombok.Data;

@Data
public class DuplicateProfileMemberRequest {

    private String userName;

    private String studentNumber;

    private String userHp;
}
