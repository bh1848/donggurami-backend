package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.admin.admin.dto.ClubListResponse;

import java.util.List;

public interface ClubRepositoryCustom {
    List<ClubListResponse> findAllWithMemberAndLeaderCount();

    void deleteClubAndDependencies(Long clubId);
}
