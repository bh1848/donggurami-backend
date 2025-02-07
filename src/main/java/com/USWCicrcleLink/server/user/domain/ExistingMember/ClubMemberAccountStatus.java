package com.USWCicrcleLink.server.user.domain.ExistingMember;

import com.USWCicrcleLink.server.club.club.domain.Club;
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
@Table(name = "CLUB_MEMBER_ACCOUNTSTATUS_TABLE")
public class ClubMemberAccountStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLUB_MEMBER_ACCOUNTSTATUS_ID")
    private Long clubMember_AccountStatus_Id;

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


