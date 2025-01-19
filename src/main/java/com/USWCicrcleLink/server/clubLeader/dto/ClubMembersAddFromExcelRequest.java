package com.USWCicrcleLink.server.clubLeader.dto;

import lombok.Data;

@Data
public class ClubMembersAddFromExcelRequest {

    private String userName;

    private String major;

    private String studentNumber;

    private String userHp;
}
