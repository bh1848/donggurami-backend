package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.ClubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubCategoryRepository extends JpaRepository<ClubCategory, Long> {
    @Query("SELECT c.clubCategoryId FROM ClubCategory c WHERE c.clubCategoryUUID = :uuid")
    Optional<Long> findClubCategoryIdByUUID(@Param("uuid") UUID uuid);
    Optional<ClubCategory> findByClubCategoryName(String clubCategoryName);

    @Query("SELECT c.clubCategoryId FROM ClubCategory c WHERE c.clubCategoryUUID IN :clubCategoryUUIDs")
    List<Long> findClubCategoryIdsByUUIDs(@Param("clubCategoryUUIDs") List<UUID> clubCategoryUUIDs);
}
