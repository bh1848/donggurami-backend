package com.USWCicrcleLink.server.club.clubIntro.repository;

import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubIntroPhotoRepository extends JpaRepository<ClubIntroPhoto, Long> {
    Optional<ClubIntroPhoto> findByClubIntro_ClubIntroIdAndOrder(Long clubIntroId, int order);
    List<ClubIntroPhoto> findAllByClubIntro_ClubIntroIdOrderByOrderAsc(Long clubIntroId);

    List<ClubIntroPhoto> findByClubIntro(ClubIntro clubIntro);

    @Query("SELECT cip.clubIntroPhotoS3Key FROM ClubIntroPhoto cip WHERE cip.clubIntro.club.clubId = :clubId")
    List<String> findS3KeysByClubId(@Param("clubId") Long clubId);
}
