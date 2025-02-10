package com.USWCicrcleLink.server.clubLeader.dto.clubMembers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExcelProfileMemberResponse {

    private String userName;

    private String studentNumber;

    private String userHp;
}
