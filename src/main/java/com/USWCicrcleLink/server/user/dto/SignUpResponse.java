package com.USWCicrcleLink.server.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) //null 값이 있는 필드는 화면 출력시 제외
@AllArgsConstructor
public class SignUpResponse {

    private UUID emailTokenId; // 이메일 토큰의 uuid
    private Object data;

    public SignUpResponse(UUID emailTokenId){
        this.emailTokenId=emailTokenId;
    }

}
