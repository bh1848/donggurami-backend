package com.USWCicrcleLink.server.clubLeader.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClubMembersDeleteRequest {

    @NotNull(message = "삭제할 동아리 회원을 선택해주세요.")
    private Long clubMemberId;
}
