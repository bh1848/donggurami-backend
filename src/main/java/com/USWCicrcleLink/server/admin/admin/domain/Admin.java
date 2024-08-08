package com.USWCicrcleLink.server.admin.admin.domain;

import com.USWCicrcleLink.server.global.login.domain.IntegratedUser;
import com.USWCicrcleLink.server.global.security.domain.Role;
import jakarta.persistence.*;
import lombok.*;
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
public class Admin implements IntegratedUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "admin_UUID", nullable = false, unique = true)
    private UUID adminUUID;

    @Column(name = "admin_account", nullable = false, unique = true)
    private String adminAccount;

    @Column(name = "admin_pw", nullable = false)
    private String adminPw;

    @Column(name = "admin_name", nullable = false, unique = true)
    private String adminName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Override
    public String getIntegratedAccount() {
        return adminAccount;
    }

    @Override
    public String getIntegratedPw() {
        return adminPw;
    }

    @Override
    public UUID getIntegratedUUID() {
        return adminUUID;
    }
}
