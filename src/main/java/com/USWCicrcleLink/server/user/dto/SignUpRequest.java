package com.USWCicrcleLink.server.user.dto;

import com.USWCicrcleLink.server.user.domain.UserTemp;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String account;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;

    @NotBlank(message = "이름 필수 입력 값입니다.")
    private String userName;

    private String telephone;

    @NotBlank(message = "학번 필수 입력 값입니다.")
    private String studentNumber;

    @NotBlank(message = "학과 필수 입력 값입니다.")
    private String major;

    @NotBlank(message = "이메일 필수 입력 값입니다.")
    private String email;

    // 이메일 인증 확인을 위한 필드
    private Boolean isEmailVerified = false;

    public UserTemp toEntity() {
        return UserTemp.builder()
                .tempAccount(account)
                .tempPw(password)
                .tempName(userName)
                .tempHp(telephone)
                .tempStudentNumber(studentNumber)
                .tempMajor(major)
                .tempEmail(email)
                .isEmailVerified(isEmailVerified)
                .build();
    }

}
