package com.USWCicrcleLink.server.club.repository;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.ClubIntro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubIntroRepository extends JpaRepository<ClubIntro, Long> {
    Optional<ClubIntro> findByClubClubId(Long clubId);
    Optional<ClubIntro> findByClub(Club club);
}
