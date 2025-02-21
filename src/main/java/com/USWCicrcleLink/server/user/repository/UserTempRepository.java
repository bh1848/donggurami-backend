package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserTempRepository extends JpaRepository<UserTemp, Long> {
    Optional<UserTemp> findByTempEmail(String email);

    Optional<UserTemp> findByTempAccount(String account);
}

