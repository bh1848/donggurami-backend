package com.USWCicrcleLink.server.clubLeaders.repository;

import com.USWCicrcleLink.server.clubLeaders.domain.Leader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LeaderRepository extends JpaRepository<Leader,Long> {
    Optional<Leader> findByLeaderUUID(UUID leaderUUID);
}
