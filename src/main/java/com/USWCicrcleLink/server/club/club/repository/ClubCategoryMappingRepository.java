package com.USWCicrcleLink.server.club.club.repository;


import com.USWCicrcleLink.server.club.club.domain.ClubCategory;
import com.USWCicrcleLink.server.club.club.domain.ClubCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubCategoryMappingRepository
        extends JpaRepository<ClubCategoryMapping,Long> {
    List<ClubCategoryMapping> findByClubCategory(ClubCategory clubCategory);
    List<ClubCategoryMapping> findByClubClubId(Long clubId);
    void deleteByClub_ClubId(Long clubId);
}
