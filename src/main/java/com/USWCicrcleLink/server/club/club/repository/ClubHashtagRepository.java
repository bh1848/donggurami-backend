package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClubHashtagRepository extends JpaRepository<ClubHashtag, Long> {
    List<ClubHashtag> findByClub(Club club);
    List<ClubHashtag> findByClubClubId(Long clubId);
    @Query("SELECT h.clubHashtag FROM ClubHashtag h WHERE h.club.clubId = :clubId")
    List<String> findHashtagsByClubId(@Param("clubId") Long clubId);
    boolean existsByClub_ClubIdAndClubHashtag(Long clubId, String clubHashtag);

    void deleteByClub_ClubIdAndClubHashtag(Long clubId, String clubHashtag);
}
