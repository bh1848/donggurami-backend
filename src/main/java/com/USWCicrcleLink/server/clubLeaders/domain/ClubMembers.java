package com.USWCicrcleLink.server.clubLeaders.domain;

import com.USWCicrcleLink.server.user.domain.User;
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
@Table(name = "CLUB_MEMBERS_TABLE")
public class ClubMembers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clubMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clubId")
    private Club club;
}
