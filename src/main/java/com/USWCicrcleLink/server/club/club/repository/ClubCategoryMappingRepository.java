package com.USWCicrcleLink.server.club.club.repository;


import com.USWCicrcleLink.server.club.club.domain.ClubCategory;
import com.USWCicrcleLink.server.club.club.domain.ClubCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClubCategoryMappingRepository
        extends JpaRepository<ClubCategoryMapping,Long> {
    List<ClubCategoryMapping> findByClubCategory(ClubCategory clubCategory);
    List<ClubCategoryMapping> findByClubClubId(Long clubId);
    List<ClubCategoryMapping> findByClub_ClubId(Long clubId);

    @Modifying
    @Query("DELETE FROM ClubCategoryMapping cm WHERE cm.clubCategory = :clubCategory")
    void deleteByClubCategory(@Param("clubCategory") ClubCategory clubCategory);
}
