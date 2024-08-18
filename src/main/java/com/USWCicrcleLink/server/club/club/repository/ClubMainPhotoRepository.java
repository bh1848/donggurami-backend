package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMainPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubMainPhotoRepository extends JpaRepository<ClubMainPhoto, Long> {
    ClubMainPhoto findByClub_ClubId(Long clubId);

    Optional<ClubMainPhoto> findByClub(Club club);
}
