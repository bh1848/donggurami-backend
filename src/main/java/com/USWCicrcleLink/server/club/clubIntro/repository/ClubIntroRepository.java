package com.USWCicrcleLink.server.club.clubIntro.repository;

import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubIntroRepository extends JpaRepository<ClubIntro, Long> {
    @Query("SELECT ci FROM ClubIntro ci WHERE ci.club.clubId = :clubId")
    Optional<ClubIntro> findByClubClubId(@Param("clubId") Long clubId);

    @Query("SELECT ci.club.clubId FROM ClubIntro ci WHERE ci.recruitmentStatus = 'OPEN'")
    List<Long> findOpenClubIds();
}
