package com.USWCicrcleLink.server.admin.repository;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.notice.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByAdminAccount(String adminAccount);
}
