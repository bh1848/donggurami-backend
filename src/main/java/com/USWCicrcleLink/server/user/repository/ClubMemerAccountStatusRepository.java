package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubMemerAccountStatusRepository extends JpaRepository<ClubMemberAccountStatus,Long> {
    Long countByClubMemberTemp_ClubMemberTempId(Long clubMemberTempId);

    // clubMemberTemp_Id로 객체 조회
    List<ClubMemberAccountStatus> findAllByClubMemberTemp_ClubMemberTempId(Long clubMemberTempId);


}
