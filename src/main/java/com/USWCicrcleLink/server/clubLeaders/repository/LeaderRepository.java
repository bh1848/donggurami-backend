package com.USWCicrcleLink.server.clubLeaders.repository;

import com.USWCicrcleLink.server.clubLeaders.domain.Leader;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderRepository extends JpaRepository<Leader, Long> {
}
