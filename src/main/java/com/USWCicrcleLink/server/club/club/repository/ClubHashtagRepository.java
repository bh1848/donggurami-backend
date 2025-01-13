package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.ClubHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubHashtagRepository extends JpaRepository<ClubHashtag, Long> {
    void deleteByClub_ClubId(Long clubId);
}
