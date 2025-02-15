package com.USWCicrcleLink.server.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// 회원이 선택한 동아리 id
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClubDTO {
    private UUID clubUUID;
}
