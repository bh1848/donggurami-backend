package com.USWCicrcleLink.server.club.repository;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.ClubMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ClubMembersRepository extends JpaRepository<ClubMembers,Long>, ClubMembersRepositoryCustom {

    List<ClubMembers> findByClub(Club club);
    @Modifying
    @Transactional
    @Query("DELETE FROM ClubMembers cm WHERE cm.club.clubId = :clubId")
    void deleteByClubClubId(@Param("clubId")Long clubId);
}
