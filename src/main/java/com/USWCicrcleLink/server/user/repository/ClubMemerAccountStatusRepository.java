package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubMemerAccountStatusRepository extends JpaRepository<ClubMemberAccountStatus,Long> {
    Long countByClubMemberTempId(Long clubMemberTempId);

    // clubMemberTemp_Id로 객체 조회
    List<ClubMemberAccountStatus> findByClubMemberTempId(Long clubMemberTemp_Id);
}
