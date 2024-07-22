package com.USWCicrcleLink.server.club.repository;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.ClubMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClubMembersRepository extends JpaRepository<ClubMembers,Long>, ClubMembersRepositoryCustom {

    List<ClubMembers> findByClub(Club club);
}
