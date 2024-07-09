package com.USWCicrcleLink.server.club.domain;

import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "CLUB_TABLE")
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private Long clubId;

    @Column(name = "club_name")
    private String clubName;

    @Column(name = "main_photo_path")
    private String mainPhotoPath;

    @Column(name = "chat_room_url")
    private String chatRoomUrl;

    private String leaderName;

    @Enumerated(EnumType.STRING)
    private Department department;

    @Column(name = "katalik_id")
    private String katalikId;

    @Column(name = "insta_url")
    private String instaUrl;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private Leader leader;

    @OneToOne(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ClubIntro clubIntro;

    private String katalkID;

    private String clubInsta;

    public void updateClubInfo(String mainPhotoPath, String chatRoomURL, String katalkID, String clubInsta) {
        this.mainPhotoPath = mainPhotoPath;
        this.chatRoomUrl = chatRoomURL;
        this.katalkID = katalkID;
        this.clubInsta = clubInsta;
    }

}
