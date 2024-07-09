package com.USWCicrcleLink.server.email.domain;

import com.USWCicrcleLink.server.user.domain.UserTemp;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "EMAILTOKEN_TABLE")
public class EmailToken {


    private static final long CERTIFICATION_TIME = 5L;

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    private String emailTokenId;

    // 이메일 토큰과 관련된 임시 회원  id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="userTempId")
    private UserTemp userTemp;

    private LocalDateTime certificationTime; // 이메일 인증 시간

    private boolean isEmailTokenExpired; // 이메일 토큰 만료 여부


    // 이메일 인증 토큰 생성
    public static EmailToken createEmailToken(UserTemp userTemp) {
        EmailToken emailToken = EmailToken.builder()
                .certificationTime(LocalDateTime.now().plusMinutes(CERTIFICATION_TIME))
                .isEmailTokenExpired(false)
                .userTemp(userTemp)
                .build();

        return emailToken;
    }

}
