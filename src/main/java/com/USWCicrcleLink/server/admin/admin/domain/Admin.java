package com.USWCicrcleLink.server.admin.admin.domain;

import com.USWCicrcleLink.server.global.security.domain.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "ADMIN_TABLE")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "admin_UUID", nullable = false, unique = true, updatable = false)
    private UUID adminUUID;

    @Column(name = "admin_account", nullable = false, unique = true)
    private String adminAccount;

    @Column(name = "admin_pw", nullable = false)
    private String adminPw;

    @Column(name = "admin_name", nullable = false)
    private String adminName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    // UUID 자동 생성
    @PrePersist
    public void generateUUID() {
        if (this.adminUUID == null) {
            this.adminUUID = UUID.randomUUID();
        }
    }
}