package com.USWCicrcleLink.server.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "USERTEMP_TABLE")
public class UserTemp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="USERTEMP_ID")
    private Long userTempId;

    private String tempAccount;

    private String tempPw;

    private String tempName;

    private String tempStudentNumber;

    private String tempHp;

    private String tempMajor;

    private String tempEmail;

    private boolean isEmailVerified; // 이메일 인증을 했는지 여부

    public void emailVerifiedSuccess(){
        isEmailVerified=true;
    }

}
