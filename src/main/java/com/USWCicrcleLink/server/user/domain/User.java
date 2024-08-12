package com.USWCicrcleLink.server.user.domain;

import com.USWCicrcleLink.server.global.security.domain.Role;
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
@Table(name = "USER_TABLE")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "uuid", unique = true, nullable = false)
    private UUID userUUID;

    @Column(unique = true, nullable = false)
    private String userAccount;

    @Column(nullable = false)
    private String userPw;

    @Column(unique = true, nullable = false)
    private String email;

    private LocalDateTime userCreatedAt;

    private LocalDateTime userUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @PrePersist
    public void prePersist() {
        this.userUUID = UUID.randomUUID();
    }

    public static User createUser(UserTemp userTemp){
        return User.builder()
                .userAccount(userTemp.getTempAccount())
                .userPw(userTemp.getTempPw())
                .email(userTemp.getTempEmail())
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .role(Role.USER)
                .build();
    }

    public void updateUserPw(String userPw){
        this.userPw = userPw;
    }

}

