package com.USWCicrcleLink.server.club.clubIntro.domain;

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
@Table(name = "CLUB_INTRO_PHOTO_TABLE")
public class ClubIntroPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_intro_photo_id")
    private Long clubIntroPhotoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_intro_id", nullable = false)
    private ClubIntro clubIntro;

    @Column(name = "club_intro_photo_path")
    private String clubIntroPhotoPath;

    @Column(name = "photo_order", nullable = false)
    private int order;

    public void updateClubIntroPhoto(ClubIntro clubIntro, String clubIntroPhotoPath, int order) {
        this.clubIntro = clubIntro;
        this.clubIntroPhotoPath = clubIntroPhotoPath;
        this.order = order;
    }
}
