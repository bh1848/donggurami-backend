package com.USWCicrcleLink.server.club.repository;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.Department;
import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.dto.ClubMembersLeaderCount;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {
    List<Club> findByDepartment(Department department);
    List<Club> findByRecruitmentStatusAndDepartment(RecruitmentStatus recruitmentStatus, Department department);
    @NonNull
    Page<Club> findAll(@NonNull Pageable pageable);
    Club findByClubId(Long clubId);
    
    //동아리 리더, 회원 수 조회
    @Query("SELECT new com.USWCicrcleLink.server.club.dto.ClubMembersLeaderCount(c.clubId, COUNT(DISTINCT cm.clubMemberId), COUNT(DISTINCT l.leaderId)) " +
            "FROM Club c " +
            "LEFT JOIN ClubMembers cm ON c.clubId = cm.club.clubId " +
            "LEFT JOIN Leader l ON c.clubId = l.club.clubId " +
            "GROUP BY c.clubId")
    List<ClubMembersLeaderCount> findAllWithMemberAndLeaderCount();
}
