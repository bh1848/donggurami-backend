package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMembers;
import com.USWCicrcleLink.server.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMembersRepository extends JpaRepository<ClubMembers,Long>, ClubMembersRepositoryCustom {

    //동아리원 조회 성능 비교
    List<ClubMembers> findByClub(Club club);
    List<ClubMembers> findByProfileProfileId(Long profileId);
    Optional<ClubMembers> findByProfileProfileIdAndClubClubId(Long profileId, Long clubId);
    Optional<ClubMembers> findByClubClubIdAndClubMemberId(Long clubId, Long memberId);
    List<ClubMembers> findByClubClubIdAndClubMemberIdIn(Long clubId, List<Long> memberId);

    void deleteAllByProfile(Profile profile);

    boolean existsByProfileAndClub_ClubId(Profile profile, Long clubId);

    @Query("SELECT cm.profile FROM ClubMembers cm WHERE cm.club.clubId = :clubId")
    List<Profile> findProfilesByClubId(@Param("clubId") Long clubId);

}
