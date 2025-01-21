package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.ClubMembers;

import com.USWCicrcleLink.server.profile.domain.MemberType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ClubMembersRepositoryCustom {
    List<ClubMembers> findAllWithProfile(Long clubId);
    List<ClubMembers> findAllWithProfileByName(Long clubId);
    List<ClubMembers> findAllWithProfileByMemberType(Long clubId, MemberType memberType);
}
