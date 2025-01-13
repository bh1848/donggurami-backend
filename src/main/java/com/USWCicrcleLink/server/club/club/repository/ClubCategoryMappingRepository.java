package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.ClubCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubCategoryMappingRepository extends JpaRepository<ClubCategoryMapping, Long> {
    void deleteByClub_ClubId(Long clubId);
}
