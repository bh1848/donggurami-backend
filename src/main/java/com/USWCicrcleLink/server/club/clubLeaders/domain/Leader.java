package com.USWCicrcleLink.server.club.clubLeaders.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "leader_pw", nullable = false)
    private String leaderPw;

}