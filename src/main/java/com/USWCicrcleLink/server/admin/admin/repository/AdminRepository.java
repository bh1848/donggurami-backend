package com.USWCicrcleLink.server.admin.admin.repository;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByAdminAccount(String adminAccount);
    Optional<Admin> findByAdminUUID(UUID adminUUID);
}
