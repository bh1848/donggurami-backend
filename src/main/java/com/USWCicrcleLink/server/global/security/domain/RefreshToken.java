package com.USWCicrcleLink.server.global.security.domain;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@Getter
@RedisHash("refreshToken")
@Builder
@AllArgsConstructor
public class RefreshToken {

    @Id
    private Long id;

    private String refreshToken;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private long expiration;
}
