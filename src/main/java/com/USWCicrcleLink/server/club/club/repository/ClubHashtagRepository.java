package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.ClubHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClubHashtagRepository extends JpaRepository<ClubHashtag, Long> {
    @Query("SELECT ch FROM ClubHashtag ch WHERE ch.club.clubId IN :clubIds")
    List<ClubHashtag> findByClubIds(@Param("clubIds") List<Long> clubIds);

    @Query("SELECT ch FROM ClubHashtag ch WHERE ch.club.clubId = :clubId")
    List<ClubHashtag> findByClubClubId(@Param("clubId") Long clubId);

    @Query("SELECT h.clubHashtag FROM ClubHashtag h WHERE h.club.clubId = :clubId")
    List<String> findHashtagsByClubId(@Param("clubId") Long clubId);

    void deleteByClub_ClubIdAndClubHashtag(Long clubId, String clubHashtag);
}
