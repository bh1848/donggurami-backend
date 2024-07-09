package com.USWCicrcleLink.server.clubLeader.dto;

import com.USWCicrcleLink.server.club.domain.Club;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecruitmentRequest {
    // token 대신 uuid(식별 용도)
    private UUID leaderUUID;
}
