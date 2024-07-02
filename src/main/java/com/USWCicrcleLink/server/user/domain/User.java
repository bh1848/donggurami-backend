package com.USWCicrcleLink.server.user.domain;

import com.USWCicrcleLink.server.profile.domain.Profile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "USER_TABLE")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String userUUID;

    private String userAccount;

    private String userPw;

    private String email;

    private LocalDateTime userCreatedAt;

    private LocalDateTime userUpdatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;

}
