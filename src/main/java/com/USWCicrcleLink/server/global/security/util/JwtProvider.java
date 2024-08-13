package com.USWCicrcleLink.server.global.security.util;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.service.CustomUserDetailsService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JWT를 생성하고 검증
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1800000L;
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 604800000L;

    private final CustomUserDetailsService customUserDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret.key}")
    private String secretKeyString;
    private Key secretKey;

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    // 엑세스 토큰 생성
    public String createAccessToken(String uuid) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(uuid);
        Role role;
        List<Long> clubIds = null;
        Long clubId = null;

        Claims claims = Jwts.claims().setSubject(uuid);
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            role = customUserDetails.user().getRole();
            clubIds = customUserDetails.getClubIds();
        } else if (userDetails instanceof CustomLeaderDetails customLeaderDetails) {
            role = customLeaderDetails.leader().getRole();
            clubId = customLeaderDetails.getClubId();
        } else if (userDetails instanceof CustomAdminDetails customAdminDetails) {
            role = customAdminDetails.admin().getRole();
        } else {
            throw new IllegalArgumentException("해당 역할 토큰 생성 불가");
        }

        claims.put("role", role.name());
        if (clubIds != null) {
            claims.put("clubIds", clubIds);
        } else if (clubId != null) {
            claims.put("clubId", clubId);
        }

        Date now = new Date();
        String accessToken = buildToken(claims, now);
        log.debug("엑세스 토큰 생성: {}", accessToken);
        return accessToken;
    }

    // 리프레시 토큰 생성 및 Redis에 저장
    public String createRefreshToken(String uuid, HttpServletResponse response) {
        // 먼저 Redis에 저장된 기존 리프레시 토큰을 삭제 (기존 토큰이 있는 경우에만)
        String existingRefreshToken = getStoredRefreshTokenByUuid(uuid);
        if (existingRefreshToken != null) {
            deleteRefreshToken(existingRefreshToken);
            log.debug("기존 리프레시 토큰 삭제 완료: {}", existingRefreshToken);
        }

        // 새 리프레시 토큰 생성
        String newToken = UUID.randomUUID().toString();

        // Redis에 새 토큰 데이터 저장
        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("_class", "com.USWRandomChat.backend.global.security.domain.RefreshToken");
        tokenData.put("uuid", uuid);
        tokenData.put("expiration", String.valueOf(REFRESH_TOKEN_EXPIRATION_TIME));
        tokenData.put("refreshToken", newToken);

        // Redis에 저장
        redisTemplate.opsForHash().putAll("refreshToken:" + newToken, tokenData);
        redisTemplate.expire("refreshToken:" + newToken, REFRESH_TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);

        // 리프레시 토큰을 쿠키로 전송
        setRefreshTokenCookie(response, newToken);

        return newToken;
    }

    // UUID로 Redis에서 리프레시 토큰 검색
    private String getStoredRefreshTokenByUuid(String uuid) {
        Set<String> keys = redisTemplate.keys("refreshToken:*");
        if (keys != null) {
            for (String key : keys) {
                String storedUuid = (String) redisTemplate.opsForHash().get(key, "uuid");
                if (uuid.equals(storedUuid)) {
                    return (String) redisTemplate.opsForHash().get(key, "refreshToken");
                }
            }
        }
        return null;
    }

    // JWT 빌드
    private String buildToken(Claims claims, Date now) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + JwtProvider.ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 엑세스 토큰에서 UUID 추출
    public String getUUIDFromAccessToken(String accessToken) {
        String uuid = getClaims(accessToken).getSubject();
        log.debug("액세스 토큰에서 추출한 UUID: {}", uuid);
        return uuid;
    }

    // 엑세스 토큰에서 role 추출
    public Role getRoleFromAccessToken(String accessToken) {
        Claims claims = getClaims(accessToken);
        Role role = Role.valueOf(claims.get("role").toString());
        log.debug("액세스 토큰에서 추출한 역할: {}", role);
        return role;
    }

    // 엑세스 토큰에서 사용자 정보 추출
    public UserDetails getUserDetails(String accessToken) {
        String uuid = getUUIDFromAccessToken(accessToken);
        Role role = getRoleFromAccessToken(accessToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUuidAndRole(uuid, role);
        log.debug("액세스 토큰에서 추출한 사용자 세부 정보: {}", userDetails);
        return userDetails;
    }

    // 엑세스 토큰 유효성 검증
    public boolean validateAccessToken(String accessToken) {
        try {
            Jws<Claims> claims = getClaimsJws(accessToken);
            boolean isValid = !claims.getBody().getExpiration().before(new Date());
            log.debug("엑세스 토큰 유효성 검증 결과: {}", isValid);
            return isValid;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("엑세스 토큰 검증 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    // JWT에서 클레임 추출
    private Jws<Claims> getClaimsJws(String accessToken) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken);
    }

    private Claims getClaims(String accessToken) {
        return getClaimsJws(accessToken).getBody();
    }

    // 헤더에서 엑세스 토큰 추출
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(BEARER_PREFIX.length());
            log.debug("요청 헤더에서 추출한 토큰: {}", token);
            return token;
        }
        return null;
    }

    // 엑세스 토큰에서 인증 정보 가져오기
    public Authentication getAuthentication(String accessToken) {
        UserDetails userDetails = getUserDetails(accessToken);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        log.debug("액세스 토큰에서 가져온 인증 정보: {}", auth);
        return auth;
    }
    
    // 레디스에 저장된 리프레시 토큰 가져오기
    public String getStoredUuidFromRefreshToken(String refreshToken) {
        return (String) redisTemplate.opsForHash().get("refreshToken:" + refreshToken, "uuid");
    }


    // 리프레시 토큰 HttpOnly 쿠키로 설정
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) REFRESH_TOKEN_EXPIRATION_TIME / 1000);

        // 쿠키 추가
        response.addCookie(refreshTokenCookie);
        response.addHeader("Set-Cookie", "refreshToken=" + refreshToken
                + "; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age="
                + refreshTokenCookie.getMaxAge());
    }

    // 쿠키에서 리프레시 토큰 추출
    public String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }
        log.warn("리프레시 토큰이 쿠키에서 발견되지 않음.");
        return null;
    }


    // 리프레시 토큰 유효성 검증
    public boolean validateRefreshToken(String refreshToken) {
        try {
            // Redis에서 Hash로 저장된 데이터 가져오기
            String storedToken = (String) redisTemplate.opsForHash().get("refreshToken:" + refreshToken, "refreshToken");

            if (storedToken == null) {
                log.warn("Redis에서 리프레시 토큰을 찾을 수 없음: {}", refreshToken);
                throw new JwtException(ExceptionType.INVALID_REFRESH_TOKEN.getMessage());
            }
            boolean isValid = storedToken.equals(refreshToken);
            log.debug("리프레시 토큰 유효성 검증 결과: {}", isValid);
            return isValid;
        } catch (JwtException e) {
            throw new JwtException(ExceptionType.INVALID_REFRESH_TOKEN.getMessage());
        }
    }

    // redis에서 리프레시 토큰 삭제
    public void deleteRefreshToken(String refreshToken) {
        redisTemplate.delete("refreshToken:" + refreshToken);
        log.debug("리프레시 토큰 삭제: {}", refreshToken);
    }

    // 쿠키에서 리프레시 토큰 삭제
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);  // 쿠키를 삭제하는 방법

        response.addCookie(refreshTokenCookie);
        log.debug("클라이언트의 쿠키에서 리프레시 토큰 삭제 완료");
    }
}
