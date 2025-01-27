package com.USWCicrcleLink.server.profile.domain;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "PROFILE_TABLE")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "user_name", nullable = false)
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "이름은 한글만 입력 가능합니다", groups = ValidationGroups.PatternGroup.class)
    private String userName;

    @Column(name = "student_number", nullable = false)
    @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력 가능 합니다", groups = ValidationGroups.PatternGroup.class)
    private String studentNumber;

    @Column(name = "user_hp", nullable = false)
    @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력 가능 합니다", groups = ValidationGroups.PatternGroup.class)
    private String userHp;

    @Column(name = "major", nullable = false)
    private String major;

    @Column(name = "profile_created_at", nullable = false)
    private LocalDateTime profileCreatedAt;

    @Column(name = "profile_updated_at", nullable = false)
    private LocalDateTime profileUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false)
    private MemberType memberType;

    @Column(name = "fcm_token")
    private String fcmToken;// 회원이 직접 로그인할 때

    @Column(name = "fcm_token_updated_at")
    private LocalDateTime fcmTokenCertificationTimestamp;

    public static Profile createProfile(UserTemp userTemp, User user) {
        return Profile.builder()
                .user(user)
                .userName(userTemp.getTempName())
                .studentNumber(userTemp.getTempStudentNumber())
                .userHp(userTemp.getTempHp())
                .major(userTemp.getTempMajor())
                .profileCreatedAt(LocalDateTime.now())
                .profileUpdatedAt(LocalDateTime.now())
                .build();
    }

    public void updateProfile(String userName, String major, String studentNumber, String userHp) {
        if (userName != null) {
            validateProfileInput("userName", userName);
            this.userName = userName;
        }
        if (major != null) {
            validateProfileInput("major", major);
            this.major = major;
        }
        if (studentNumber != null) {
            validateProfileInput("studentNumber", studentNumber);
            this.studentNumber = studentNumber;
        }
        if (userHp != null) {
            validateProfileInput("userHp", userHp);
            this.userHp = userHp;
        }

        this.profileUpdatedAt = LocalDateTime.now();
    }


    private void validateProfileInput(String fieldName, String fieldValue) {
        if (fieldValue == null || fieldValue.trim().isEmpty()) {
            throw new ProfileException(ExceptionType.INVALID_INPUT);
        }
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void updateFcmTokenTime(String fcmToken, LocalDateTime fcmTokenCertificationTimestamp) {
        this.fcmToken = fcmToken;
        this.fcmTokenCertificationTimestamp = fcmTokenCertificationTimestamp;
    }
}