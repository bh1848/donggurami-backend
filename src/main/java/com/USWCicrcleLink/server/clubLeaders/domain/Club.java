package com.USWCicrcleLink.server.clubLeaders.domain;

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
@Table(name = "CLUB_TABLE")
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clubId;

    private String clubName;

    private String leaderName;

    @Enumerated(EnumType.STRING)
    private Department department;

    private String mainPhotoPath;

    private String chatRoomURL;

    private String katalkID;

    private String clubInsta;

}
