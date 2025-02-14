package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.ClubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubCategoryRepository extends JpaRepository<ClubCategory, Long> {
    Optional<ClubCategory> findByClubCategoryUUID(UUID clubCategoryUUID);
    Optional<ClubCategory> findByClubCategoryName(String clubCategoryName);

    List<ClubCategory> findByClubCategoryUUIDIn(List<UUID> clubCategoryUUIDs);
}
