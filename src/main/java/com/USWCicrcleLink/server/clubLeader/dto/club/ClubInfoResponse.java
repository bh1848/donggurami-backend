package com.USWCicrcleLink.server.clubLeader.dto.club;

import com.USWCicrcleLink.server.club.club.domain.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubInfoResponse {

    private String mainPhotoUrl;

    private String clubName;

    private String leaderName;

    private String leaderHp;

    private String clubInsta;

    private String clubRoomNumber;

    private List<String> clubHashtag;

    private List<String> clubCategory;

    private Department department;

}
