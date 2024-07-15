package com.USWCicrcleLink.server.admin.club.repository;

import com.USWCicrcleLink.server.admin.club.domain.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByAdminAccount(String adminAccount);
}
