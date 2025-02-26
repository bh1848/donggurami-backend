package com.USWCicrcleLink.server.user.dto;

import com.USWCicrcleLink.server.global.bucket4j.ClientIdentifier;
import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogInRequest implements ClientIdentifier {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String account;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;

    private String fcmToken;

    @Override
    public String getClientId() {
        return this.account;
    }
}
