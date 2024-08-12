package com.USWCicrcleLink.server.club.clubIntro.repository;

import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubIntroRepository extends JpaRepository<ClubIntro, Long> {
    Optional<ClubIntro> findByClubClubId(Long clubId);
}
