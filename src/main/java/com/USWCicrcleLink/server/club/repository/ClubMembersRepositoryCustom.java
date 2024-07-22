package com.USWCicrcleLink.server.club.repository;

import com.USWCicrcleLink.server.club.domain.ClubMembers;

import java.util.List;

public interface ClubMembersRepositoryCustom {
    List<ClubMembers> findAllWithProfile(Long clubId);
}
