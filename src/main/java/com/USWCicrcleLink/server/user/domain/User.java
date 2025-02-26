package com.USWCicrcleLink.server.user.domain;

import com.USWCicrcleLink.server.global.bucket4j.ClientIdentifier;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class User implements ClientIdentifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "uuid", unique = true, nullable = false)
    private UUID userUUID;

    @Column(name = "user_account", unique = true, nullable = false)
    @NotBlank(message = "아이디는 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 5, max = 20, message = "아이디는 5~20자 이내여야 합니다.",groups = ValidationGroups.SizeGroup.class )
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문 대소문자 및 숫자만 가능합니다.",groups = ValidationGroups.PatternGroup.class)
    private String userAccount;

    @Column(name = "user_pw", nullable = false)
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    private String userPw;

    @Column(unique = true, nullable = false)
    private String email;

    private LocalDateTime userCreatedAt;

    private LocalDateTime userUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;


    public static User createUser(UUID signupUUID,SignUpRequest request,String encodedPw,String email){
        return User.builder()
                .userUUID(signupUUID)
                .userAccount(request.getAccount())
                .userPw(encodedPw)
                .email(email)
                .role(Role.USER)
                .build();
    }

    @PrePersist
    public void prePersist() {
        this.userCreatedAt = LocalDateTime.now();
        this.userUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.userUpdatedAt = LocalDateTime.now();
    }

    public void updateUserPw(String userPw){
        this.userPw = userPw;
    }

    @Override
    public String getClientId() {
        return this.email;
    }
}

