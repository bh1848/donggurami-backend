package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthTokenRepository extends JpaRepository<AuthToken,Long> {
    Optional<AuthToken> findByUserUserUUID(UUID uuid);
}
