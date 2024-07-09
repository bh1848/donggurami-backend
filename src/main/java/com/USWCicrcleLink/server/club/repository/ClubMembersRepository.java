package com.USWCicrcleLink.server.club.repository;

import com.USWCicrcleLink.server.club.domain.ClubMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClubMembersRepository extends JpaRepository<ClubMembers,Long> {
    List<ClubMembers> findByUserUserId(Long userId);
}
