package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.Department;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long>, ClubRepositoryCustom{
    List<Club> findByDepartment(Department department);
    List<Club> findByRecruitmentStatusAndDepartment(RecruitmentStatus recruitmentStatus, Department department);
    @NonNull
    Page<Club> findAll(@NonNull Pageable pageable);
}
