package com.USWCicrcleLink.server.club.clubLeaders.repository;

import com.USWCicrcleLink.server.club.clubLeaders.domain.Leader;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderRepository extends JpaRepository<Leader, Long> {
}
