package com.USWCicrcleLink.server.clubLeader.domain;

import com.USWCicrcleLink.server.club.club.domain.Club;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "LEADER_TABLE")
public class Leader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaderId;

    @Column(name = "leader_account", nullable = false, unique = true)
    private String leaderAccount;

    @Column(name = "leader_uuid",nullable = false, updatable = false)
    private String leaderUUID;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @Column(name = "leader_pw", nullable = false)
    private String leaderPw;
    @PrePersist
    public void prePersist() {
        this.leaderUUID = UUID.randomUUID().toString();
    }
}