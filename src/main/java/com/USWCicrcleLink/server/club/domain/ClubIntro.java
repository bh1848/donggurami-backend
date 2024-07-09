package com.USWCicrcleLink.server.club.domain;

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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitmentStatus recruitmentStatus = RecruitmentStatus.CLOSE;

    public void updateClubIntro(Club club, String introContent, String introPhotoPath,
                                String additionalPhotoPath1, String additionalPhotoPath2) {
        this.club = club;
        this.introContent = introContent;
        this.introPhotoPath = introPhotoPath;
        this.additionalPhotoPath1 = additionalPhotoPath1;
        this.additionalPhotoPath2 = additionalPhotoPath2;
    }

    public void toggleRecruitmentStatus() {
        // 현재 모집 상태와 반대로
        this.recruitmentStatus = this.recruitmentStatus.toggle();
    }
}
