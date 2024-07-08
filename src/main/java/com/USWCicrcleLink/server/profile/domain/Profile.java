package com.USWCicrcleLink.server.profile.domain;

import com.USWCicrcleLink.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "major", nullable = false)
    private String major;

    @Column(name = "student_number", nullable = false)
    private String studentNumber;

    @Column(name = "user_hp", nullable = false)
    private String userHp;

    @Column(name = "profile_created_at", nullable = false)
    private LocalDateTime profileCreatedAt;

    @Column(name = "profile_updated_at", nullable = false)
    private LocalDateTime profileUpdatedAt;
}
