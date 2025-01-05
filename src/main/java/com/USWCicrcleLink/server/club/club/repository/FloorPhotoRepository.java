package com.USWCicrcleLink.server.club.club.repository;


import com.USWCicrcleLink.server.club.club.domain.FloorPhoto;
import com.USWCicrcleLink.server.club.club.domain.FloorPhotoEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FloorPhotoRepository extends JpaRepository<FloorPhoto, Long> {
    Optional<FloorPhoto> findByFloor(FloorPhotoEnum floor);
}
