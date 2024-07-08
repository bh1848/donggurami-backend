package com.USWCicrcleLink.server.admin.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ADMIN_TABLE")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long id;

    @Column(name = "admin_account", nullable = false, unique = true)
    private String adminAccount;

    @Column(name = "admin_pw", nullable = false)
    private String adminPw;
}
