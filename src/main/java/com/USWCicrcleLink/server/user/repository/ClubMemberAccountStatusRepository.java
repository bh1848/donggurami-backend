package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubMemberAccountStatusRepository extends JpaRepository<ClubMemberAccountStatus,Long>, ClubMemberAccountStatusCustomRepository {
    Long countByClubMemberTemp_ClubMemberTempId(Long clubMemberTempId);
    List<ClubMemberAccountStatus> findAllByClubMemberTemp_ClubMemberTempId(Long clubMemberTempId);
    Optional<ClubMemberAccountStatus> findByClubMemberAccountStatusIdAndClub_ClubId(Long clubMemberAccountStatusId, Long clubId);
    Optional<ClubMemberAccountStatus> findByClubMemberAccountStatusUUIDAndClub_ClubUUID(UUID clubMemberAccountStatusUUID, UUID clubUUID);

}