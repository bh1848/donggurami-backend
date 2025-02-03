package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.ClubMemberAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubMemberAccountStatusRepository extends JpaRepository<ClubMemberAccountStatus,Long>, ClubMemberAccountStatusCustomRepository {
    Long countByClubMemberTempId(Long clubMemberTempId);

    Optional<ClubMemberAccountStatus> findByIdAndClubClubId(Long clubMemberAccountStatusId, Long clubId);
}
