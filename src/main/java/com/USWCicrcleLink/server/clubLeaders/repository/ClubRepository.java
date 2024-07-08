package com.USWCicrcleLink.server.clubLeaders.repository;

import com.USWCicrcleLink.server.clubLeaders.domain.Club;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Long> {
//    Club findByClubId(Club club);
}
