package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.ClubMemberAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMemerAccountStatusRepository extends JpaRepository<ClubMemberAccountStatus,Long> {


    Long countByClubMemberTempId(Long clubMemberTempId);
}
