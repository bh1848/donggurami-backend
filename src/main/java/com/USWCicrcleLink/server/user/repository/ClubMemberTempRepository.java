package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberTemp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ClubMemberTempRepository extends JpaRepository<ClubMemberTemp,Long> {
    List<ClubMemberTemp> findAllByClubMemberTempExpiryDateBefore(LocalDateTime dateTime);

}
