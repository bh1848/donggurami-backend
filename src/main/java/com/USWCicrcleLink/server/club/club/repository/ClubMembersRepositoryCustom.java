package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.ClubMembers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ClubMembersRepositoryCustom {
    List<ClubMembers> findAllWithProfile(Long clubId);
    Page<ClubMembers> findAllWithProfileByClubId(Long clubId, Pageable pageable);

    void deleteByClubClubId(Long clubId);
}
