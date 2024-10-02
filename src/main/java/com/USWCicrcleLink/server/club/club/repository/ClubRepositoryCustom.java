package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.admin.admin.dto.ClubAdminListResponse;

import java.util.List;

public interface ClubRepositoryCustom {
    List<ClubAdminListResponse> findAllWithMemberAndLeaderCount();

    void deleteClubAndDependencies(Long clubId);
}
