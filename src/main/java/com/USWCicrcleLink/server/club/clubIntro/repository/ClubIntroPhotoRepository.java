package com.USWCicrcleLink.server.club.clubIntro.repository;

import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubIntroPhotoRepository extends JpaRepository<ClubIntroPhoto, Long> {
    Optional<ClubIntroPhoto> findByClubIntro_ClubIntroIdAndOrder(Long clubIntroId, int order);
    List<ClubIntroPhoto> findAllByClubIntro_ClubIntroIdOrderByOrderAsc(Long clubIntroId);
}
