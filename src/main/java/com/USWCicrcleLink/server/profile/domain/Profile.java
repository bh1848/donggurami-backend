package com.USWCicrcleLink.server.profile.domain;

import com.USWCicrcleLink.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "PROFILE_TABLE")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    // pk 설정
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="userId")
    private User user;

    private String userName;

    private String studentNumber;

    private String userHp;

    private String major;

    private LocalDateTime profileCreatedAt;

    private LocalDateTime profileUpdatedAt;

}
