package com.USWCicrcleLink.server.global.security.jwt;

import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.global.security.details.service.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JWT를 생성하고 검증
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1800000L; // 30분
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 604800000L; // 7일

    private final CustomUserDetailsService customUserDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret.key}")
    private String secretKeyString;

    @Value("${security.cookie.secure}")
    private boolean secureCookie; // prod 환경에서는 true
    private Key secretKey;

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 엑세스 토큰 생성 및 응답 헤더에 추가
     */
    public String createAccessToken(UUID uuid, HttpServletResponse response) {
        UserDetails userDetails = customUserDetailsService.loadUserByUuid(uuid);

        Claims claims = Jwts.claims().setSubject(uuid.toString());

        UUID clubUUID = null;
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            clubUUID = customUserDetails.getClubUUIDs().isEmpty() ? null : customUserDetails.getClubUUIDs().get(0);
        } else if (userDetails instanceof CustomLeaderDetails customLeaderDetails) {
            clubUUID = customLeaderDetails.getClubUUID();
        }

        if (clubUUID != null) {
            claims.put("clubUUID", clubUUID.toString());
        }

        Date now = new Date();
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        response.setHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);
        return accessToken;
    }

    /**
     * 엑세스 토큰에서 UUID 추출
     */
    public UUID getUUIDFromAccessToken(String accessToken) {
        String uuidStr = getClaims(accessToken).getSubject();
        return UUID.fromString(uuidStr);
    }

    /**
     * 엑세스 토큰에서 사용자 정보 추출
     */
    public UserDetails getUserDetails(String accessToken) {
        UUID uuid = getUUIDFromAccessToken(accessToken);
        return customUserDetailsService.loadUserByUuid(uuid);
    }

    /**
     * 엑세스 토큰 유효성 검증
     */
    public boolean validateAccessToken(String accessToken) {
        try {
            Jws<Claims> claims = getClaimsJws(accessToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> getClaimsJws(String accessToken) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken);
    }


    // JWT Claims 파싱
    public Claims getClaims(String jwtToken) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    /**
     * 헤더에서 엑세스 토큰 추출
     */
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 엑세스 토큰에서 인증 정보 가져오기
     */
    public Authentication getAuthentication(String accessToken) {
        UserDetails userDetails = getUserDetails(accessToken);

        // userDetails.getAuthorities()` 활용하여 역할 정보 가져오기
        List<GrantedAuthority> authorities = userDetails.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    /**
     * 리프레시 토큰 생성 (Admin & Leader는 Redis, User는 JWT)
     */
    public String createRefreshToken(UUID uuid, HttpServletResponse response, boolean isUser) {
        String refreshToken;
        if (isUser) {
            refreshToken = Jwts.builder()
                    .setSubject(uuid.toString())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();
        } else {
            deleteRefreshToken(uuid);

            refreshToken = UUID.randomUUID().toString();
            String redisKey = "refreshToken:" + refreshToken;
            redisTemplate.opsForValue().set(redisKey, uuid.toString(), REFRESH_TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
        }
        setRefreshTokenCookie(response, refreshToken);
        return refreshToken;
    }

    /**
     * 리프레시 토큰 검증 (Admin & Leader는 Redis, User는 JWT)
     */
    public boolean validateRefreshToken(String refreshToken, boolean isUser) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.debug("리프레시 토큰이 존재하지 않음 - 검증 실패");
            return false;
        }

        if (isUser) {
            // 일반 사용자는 JWT 기반 검증
            try {
                getClaims(refreshToken);
                return true;
            } catch (JwtException e) {
                log.debug("JWT 검증 실패: {}", e.getMessage());
                return false;
            }
        }

        // Admin 및 Leader는 Redis 기반 검증 (파이프라인 사용)
        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            byte[] blacklistKey = serializer.serialize("blacklist:" + refreshToken);
            byte[] refreshTokenKey = serializer.serialize("refreshToken:" + refreshToken);

            // 블랙리스트 여부 조회
            assert blacklistKey != null;
            connection.stringCommands().get(blacklistKey);
            // 토큰 존재 여부 조회
            assert refreshTokenKey != null;
            connection.stringCommands().get(refreshTokenKey);
            return null;
        });

        boolean isBlacklisted = results.get(0) != null;
        boolean existsInRedis = results.get(1) != null;

        if (isBlacklisted) {
            log.debug("리프레시 토큰 검증 실패 - 블랙리스트에 등록된 토큰: {}", refreshToken);
            return false;
        }
        return existsInRedis;
    }

    /**
     * 리프레시 토큰에서 UUID 추출 (Admin & Leader는 Redis, User는 JWT)
     */
    public UUID getUUIDFromRefreshToken(String refreshToken, boolean isUser) {
        if (isUser) {
            return UUID.fromString(getClaims(refreshToken).getSubject());
        }
        String redisKey = "refreshToken:" + refreshToken;
        String uuidStr = redisTemplate.opsForValue().get(redisKey);
        return (uuidStr != null) ? UUID.fromString(uuidStr) : null;
    }

    /**
     * 리프레시 토큰 삭제 (Admin & Leader)
     */
    public void deleteRefreshToken(UUID uuid) {
        log.debug("UUID {} 기반으로 리프레시 토큰 삭제 실행", uuid);
        ScanOptions scanOptions = ScanOptions.scanOptions().match("refreshToken:*").count(100).build();
        List<byte[]> keysToDelete = new ArrayList<>();
        RedisSerializer<String> serializer = redisTemplate.getStringSerializer();

        redisTemplate.executeWithStickyConnection(connection -> {
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(scanOptions)) {
                while (cursor.hasNext()) {
                    byte[] key = cursor.next();
                    byte[] storedValueBytes = connection.stringCommands().get(key);
                    if (storedValueBytes != null) {
                        String storedUuid = serializer.deserialize(storedValueBytes);
                        if (uuid.toString().equals(storedUuid)) {
                            keysToDelete.add(key);
                        }
                    }
                }
            }
            if (!keysToDelete.isEmpty()) {
                for (byte[] key : keysToDelete) {
                    connection.keyCommands().del(key);
                }
                log.debug("삭제된 리프레시 토큰 개수: {}", keysToDelete.size());
            } else {
                log.debug("삭제할 리프레시 토큰이 없음 - UUID: {}", uuid);
            }
            return null;
        });
    }

    /**
     * 리프레시 토큰 블랙리스트 등록 (Admin & Leader)
     */
    public void blacklistRefreshToken(String refreshToken) {
        String tokenKey = "refreshToken:" + refreshToken;
        Long remainingTime = redisTemplate.getExpire(tokenKey, TimeUnit.MILLISECONDS);
        if (remainingTime == null || remainingTime <= 0) {
            log.debug("블랙리스트 등록 실패 - 토큰 만료됨: {}", refreshToken);
            return;
        }
        redisTemplate.executePipelined((RedisCallback<Void>) connection -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            byte[] blacklistKey = serializer.serialize("blacklist:" + refreshToken);
            byte[] value = serializer.serialize("invalid");
            assert blacklistKey != null;
            assert value != null;
            connection.stringCommands().setEx(blacklistKey, remainingTime / 1000, value);
            return null;
        });
        log.debug("리프레시 토큰 블랙리스트 등록 완료: {}", refreshToken);
    }

    /**
     * 쿠키에서 리프레시 토큰 추출
     */
    public String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 리프레시 토큰을 HttpOnly 쿠키로 설정 (SameSite=Strict 적용)
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        int maxAge = (int) (REFRESH_TOKEN_EXPIRATION_TIME / 1000);
        // secureCookie 값이 true면 "; Secure" 옵션을 추가합니다.
        String secureFlag = secureCookie ? "; Secure" : "";
        String cookieValue = String.format("refreshToken=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=Strict%s",
                refreshToken, maxAge, secureFlag);
        response.setHeader("Set-Cookie", cookieValue);
    }

    /**
     * 쿠키에서 리프레시 토큰 삭제 (SameSite=Strict 적용)
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        String cookieValue = "refreshToken=; Path=/; HttpOnly; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Strict";
        response.setHeader("Set-Cookie", cookieValue);
        log.debug("클라이언트 쿠키에서 리프레시 토큰 삭제 완료");
    }
}