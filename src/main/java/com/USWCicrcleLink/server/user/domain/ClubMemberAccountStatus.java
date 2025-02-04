package com.USWCicrcleLink.server.user.domain;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.global.security.domain.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "CLUB_MEMBER_ACCOUNTSTATUS_TABLE")
public class ClubMemberAccountStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLUB_MEMBER_ACCOUNTSTATUS_ID")
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "CLUBMEMBERTEMP_ID", nullable = false)
    private ClubMemberTemp clubMemberTemp;

    public static ClubMemberAccountStatus createClubMemberAccountStatus(Club club, ClubMemberTemp clubMemberTemp) {
        return ClubMemberAccountStatus.builder()
                .club(club)
                .clubMemberTemp(clubMemberTemp)
                .build();
    }

}


