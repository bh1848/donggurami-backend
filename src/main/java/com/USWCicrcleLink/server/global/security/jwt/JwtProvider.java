package com.USWCicrcleLink.server.global.security.jwt;

import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
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

    public Claims getClaims(String accessToken) {
        return getClaimsJws(accessToken).getBody();
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
     * 리프레시 토큰 생성 및 저장 (ADMIN, LEADER → UUID, USER → JWT)
     */
    public String createRefreshToken(UUID uuid, HttpServletResponse response, Role role) {
        deleteRefreshTokensByUuid(uuid);

        String refreshToken;
        if (role == Role.ADMIN || role == Role.LEADER) {
            // ADMIN, LEADER UUID 기반 토큰 사용
            refreshToken = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(
                    "refreshToken:" + refreshToken,
                    uuid.toString(),
                    REFRESH_TOKEN_EXPIRATION_TIME,
                    TimeUnit.MILLISECONDS
            );
        } else {
            // USER JWT 기반 리프레시 토큰 사용
            Date now = new Date();
            refreshToken = Jwts.builder()
                    .setSubject(uuid.toString())
                    .setIssuedAt(now)
                    .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME))
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();
        }

        // 리프레시 토큰을 HttpOnly 쿠키에 저장
        setRefreshTokenCookie(response, refreshToken);
        return refreshToken;
    }

    /**
     * 리프레시 토큰 유효성 검증
     */
    public boolean validateRefreshToken(String refreshToken, Role role) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.debug("리프레시 토큰 없음 - 검증 건너뜀");
            return false;
        }

        // 블랙리스트 체크
        if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + refreshToken))) {
            log.warn("리프레시 토큰 검증 실패 - 블랙리스트 등록된 토큰: {}", refreshToken);
            return false;
        }

        return isAdminOrLeader(role) ? validateUuidRefreshToken(refreshToken) : validateJwtRefreshToken(refreshToken);
    }

    // UUID 기반 리프레시 토큰 검증 (Redis에서 존재 확인) (Admin, Leader)
    private boolean validateUuidRefreshToken(String refreshToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("refreshToken:" + refreshToken));
    }

    // JWT 기반 리프레시 토큰 검증 (User)
    private boolean validateJwtRefreshToken(String refreshToken) {
        try {
            getClaims(refreshToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 리프레시 토큰 기반으로 UUID 조회
     */
    public UUID getUUIDFromRefreshToken(String refreshToken, Role role) {
        if (isAdminOrLeader(role)) {
            return getUuidFromUuidRefreshToken(refreshToken);
        } else {
            return getUuidFromJwtRefreshToken(refreshToken);
        }
    }

    // Redis에서 UUID 조회 (Admin, Leader)
    private UUID getUuidFromUuidRefreshToken(String refreshToken) {
        String uuidStr = redisTemplate.opsForValue().get("refreshToken:" + refreshToken);
        return uuidStr != null ? UUID.fromString(uuidStr) : null;
    }

    // JWT에서 UUID 추출 (User)
    private UUID getUuidFromJwtRefreshToken(String refreshToken) {
        return UUID.fromString(getClaims(refreshToken).getSubject());
    }

    /**
     * UUID 기반으로 Redis에서 기존 리프레시 토큰 삭제 (ADMIN, LEADER)
     */
    public void deleteRefreshTokensByUuid(UUID uuid) {
        Objects.requireNonNull(redisTemplate.keys("refreshToken:*")).stream()
                .filter(key -> uuid.toString().equals(redisTemplate.opsForValue().get(key)))
                .findFirst()
                .ifPresent(refreshTokenKey -> {
                    String refreshToken = refreshTokenKey.replace("refreshToken:", "");
                    blacklistRefreshToken(refreshToken, Role.ADMIN);
                    redisTemplate.delete(refreshTokenKey);
                    log.debug("기존 리프레시 토큰 삭제 및 블랙리스트 추가 완료: {}", refreshToken);
                });
    }

    /**
     * 리프레시 토큰 블랙리스트 등록
     */
    public void blacklistRefreshToken(String refreshToken, Role role) {
        Long remainingTime = isAdminOrLeader(role) ? getRemainingTimeForUuidToken(refreshToken)
                : getRemainingTimeForJwtToken(refreshToken);

        if (remainingTime == null || remainingTime <= 0) {
            log.warn("블랙리스트 등록 실패 - 해당 토큰이 존재하지 않거나 이미 만료됨: {}", refreshToken);
            return;
        }

        redisTemplate.opsForValue().set("blacklist:" + refreshToken, "invalid", remainingTime, TimeUnit.MILLISECONDS);
        log.debug("리프레시 토큰 블랙리스트 등록 완료: {}", refreshToken);
    }


    // UUID 기반 리프레시 토큰의 만료 시간 조회 (Admin, Leader)
    private Long getRemainingTimeForUuidToken(String refreshToken) {
        return redisTemplate.getExpire("refreshToken:" + refreshToken, TimeUnit.MILLISECONDS);
    }


    // JWT 기반 리프레시 토큰의 만료 시간 조회 (User)
    private Long getRemainingTimeForJwtToken(String refreshToken) {
        try {
            Date expiration = getClaims(refreshToken).getExpiration();
            return expiration.getTime() - System.currentTimeMillis();
        } catch (JwtException e) {
            log.warn("블랙리스트 등록 실패 - 잘못된 JWT: {}", refreshToken);
            return null;
        }
    }

    private boolean isAdminOrLeader(Role role) {
        return role == Role.ADMIN || role == Role.LEADER;
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
     * 리프레시 토큰을 HttpOnly 쿠키로 설정
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        response.setHeader("Set-Cookie",
                String.format("refreshToken=%s; Path=/; HttpOnly; Max-Age=%d; %s; SameSite=Strict",
                        refreshToken, (REFRESH_TOKEN_EXPIRATION_TIME / 1000),
                        secureCookie ? "Secure" : ""));
    }

    /**
     * 쿠키에서 리프레시 토큰 삭제
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        response.setHeader("Set-Cookie",
                "refreshToken=; Path=/; HttpOnly; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Strict");
        log.debug("클라이언트의 쿠키에서 리프레시 토큰 삭제 완료");
    }
}