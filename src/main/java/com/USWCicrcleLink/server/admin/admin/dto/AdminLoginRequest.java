package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.global.Integration.domain.LoginType;
import com.USWCicrcleLink.server.global.bucket4j.ClientIdentifier;
import com.USWCicrcleLink.server.global.validation.EnumFormat;
import com.USWCicrcleLink.server.global.validation.Sanitize;
import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminLoginRequest implements ClientIdentifier {
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Sanitize
    private String adminAccount;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Sanitize
    private String adminPw;

    @NotNull(message = "로그인 타입은 필수 입력 값입니다.")
    @EnumFormat(enumClass = LoginType.class)
    private LoginType loginType;

    @Override
    public String getClientId() {
        return this.adminAccount;
    }
}
