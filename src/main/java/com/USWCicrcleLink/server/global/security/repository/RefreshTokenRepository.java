package com.USWCicrcleLink.server.global.security.repository;

import com.USWCicrcleLink.server.global.security.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
}
