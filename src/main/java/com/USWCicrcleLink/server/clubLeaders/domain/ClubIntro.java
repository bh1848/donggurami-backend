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
@Table(name = "CLUB_INTRO_TABLE")
public class ClubIntro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clubIntroId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clubId")
    private Club club;

    private String clubIntro;

    private String introPhotoPath;

    private String additionalPhotoPath1;

    private String additionalPhotoPath2;

    @Enumerated(EnumType.STRING)
    private RecruitmentStatus recruitmentStatus;

    private String aplctFormURL;

}
