package com.USWCicrcleLink.server.profile.domain;

import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "PROFILE_TABLE")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "student_number", nullable = false)
    private String studentNumber;

    @Column(name = "user_hp", nullable = false)
    private String userHp;

    @Column(name = "major", nullable = false)
    private String major;

    @Column(name = "profile_created_at", nullable = false)
    private LocalDateTime profileCreatedAt;

    @Column(name = "profile_updated_at", nullable = false)
    @Setter
    private LocalDateTime profileUpdatedAt;


    // Profile 객체 생성
    public static Profile createProfile(UserTemp userTemp, User user){
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

}