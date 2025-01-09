package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClubHashtagRepository extends JpaRepository<ClubHashtag, Long> {
    List<ClubHashtag> findByClub(Club club);
}
