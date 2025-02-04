package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.ClubMemberAccountStatus;

import java.util.List;

public interface ClubMemberAccountStatusCustomRepository {
    List<ClubMemberAccountStatus> findAllWithClubMemberTemp(Long clubId);
}
