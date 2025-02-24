package com.USWCicrcleLink.server.email.domain;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.EmailException;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
@Table(name = "EMAIL_TOKEN_TABLE")
public class EmailToken {

    // 이메일 토큰 만료 시간 5분
    private static final long EMAIL_TOKEN_CERTIFICATION_TIME_VALUE = 5L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_token_id", nullable = false)
    private Long emailtokenId;

    @Column(name = "email_token_uuid", unique = true, nullable = false)
    private UUID emailTokenUUID;

    // 이메일 토큰과 관련된 임시 회원  id
    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name="user_temp_id",unique = true)
    private UserTemp userTemp;

    // 이메일 토큰 만료시간
    private LocalDateTime certificationTime;

    // 이메일 인증 토큰 생성
    public static EmailToken createEmailToken(UserTemp userTemp) {
        return EmailToken.builder()
                .emailTokenUUID(UUID.randomUUID())
                .userTemp(userTemp)
                .certificationTime(LocalDateTime.now().plusMinutes(EMAIL_TOKEN_CERTIFICATION_TIME_VALUE))
                .build();
    }

    // 토큰 만료 시간 검증
    public boolean isValidTime() {
        return !LocalDateTime.now().isAfter(certificationTime);
    }

    // 토큰이 만료되었는지 검증 및 처리
    public void verifyExpiredTime() {
        if (!isValidTime()) { // 만료시간이 지난 토큰인 경우
            log.error("해당 이메일 토큰의 만료시간이 지났습니다");
            throw new EmailException(ExceptionType.EMAIL_TOKEN_IS_EXPIRED);
        }
    }

    // 이메일 재인증시 필드값 업데이트
    public void updateExpiredToken() {
        this.certificationTime = LocalDateTime.now().plusMinutes(EMAIL_TOKEN_CERTIFICATION_TIME_VALUE);
    }

}
