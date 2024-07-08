package com.USWCicrcleLink.server.clubLeaders.repository;

import com.USWCicrcleLink.server.clubLeaders.domain.Club;
import com.USWCicrcleLink.server.clubLeaders.domain.Department;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {
    List<Club> findByDepartment(Department department);

    @NonNull
    Page<Club> findAll(@NonNull Pageable pageable);
}
