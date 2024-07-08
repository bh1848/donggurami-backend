package com.USWCicrcleLink.server.clubLeaders.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "CLUB_INTRO_TABLE")
public class ClubIntro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_intro_id")
    private Long clubIntroId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(name = "intro_content")
    private String introContent;

    @Column(name = "intro_photo_path")
    private String introPhotoPath;

    @Column(name = "additional_photo_path1")
    private String additionalPhotoPath1;

    @Column(name = "additional_photo_path2")
    private String additionalPhotoPath2;

    @Column(name = "googleForm_url")
    private String googleFormUrl;

    @Column(name = "recruitment_start_date")
    private LocalDate recruitmentStartDate;

    @Column(name = "recruitment_end_date")
    private LocalDate recruitmentEndDate;
}