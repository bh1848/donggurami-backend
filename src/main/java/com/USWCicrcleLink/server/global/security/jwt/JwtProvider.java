package com.USWCicrcleLink.server.global.security.jwt;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
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

        log.debug("엑세스 토큰 생성: {}", accessToken);

        response.setHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);
        return accessToken;
    }

    /**
     * 리프레시 토큰 생성 및 Redis에 저장
     */
    public String createRefreshToken(UUID uuid, HttpServletResponse response) {
        deleteRefreshTokensByUuid(uuid);

        String refreshToken = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("refreshToken:" + refreshToken, uuid.toString(), REFRESH_TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);

        setRefreshTokenCookie(response, refreshToken);

        return refreshToken;
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

    private Claims getClaims(String accessToken) {
        return getClaimsJws(accessToken).getBody();
    }

    /**
     * 헤더에서 엑세스 토큰 추출
     */
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(BEARER_PREFIX.length());
            log.debug("요청 헤더에서 추출한 액세스 토큰: {}", token);
            return token;
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
     * 리프레시 토큰 유효성 검증
      */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            // Redis에 해당 키가 존재하는지 확인 (boolean 값 안전 처리)
            boolean isValid = Boolean.TRUE.equals(redisTemplate.hasKey("refreshToken:" + refreshToken));

            if (!isValid) {
                log.warn("리프레시 토큰 검증 실패 - 해당 토큰이 Redis에 존재하지 않음: {}", refreshToken);
            } else {
                log.debug("리프레시 토큰 검증 성공 - 유효한 토큰: {}", refreshToken);
            }

            return isValid;
        } catch (Exception e) {
            log.error("리프레시 토큰 검증 중 예외 발생: {}", e.getMessage(), e);
            throw new JwtException(ExceptionType.INVALID_REFRESH_TOKEN.getMessage(), e);
        }
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
     * 리프레시 토큰 기반으로 UUID 조회
     */
    public UUID getUUIDFromRefreshToken(String refreshToken) {
        String uuidStr = redisTemplate.opsForValue().get("refreshToken:" + refreshToken);
        return uuidStr != null ? UUID.fromString(uuidStr) : null;
    }

    /**
     * 리프레시 토큰을 HttpOnly 쿠키로 설정
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        response.addHeader("Set-Cookie", "refreshToken=" + refreshToken + "; Path=/; HttpOnly; Max-Age=" + (REFRESH_TOKEN_EXPIRATION_TIME / 1000) + "; Secure; SameSite=Lax");
    }

    /**
     * UUID 기반으로 Redis에서 기존 리프레시 토큰 삭제
     */
    public void deleteRefreshTokensByUuid(UUID uuid) {
        Set<String> keys = redisTemplate.keys("refreshToken:*");
        if (keys != null) {
            for (String key : keys) {
                String storedUuid = redisTemplate.opsForValue().get(key);
                if (uuid.toString().equals(storedUuid)) {
                    redisTemplate.delete(key);
                    log.debug("기존 리프레시 토큰 삭제 완료: {}", key);
                }
            }
        }
    }

    /**
     * 쿠키에서 리프레시 토큰 삭제
      */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        String cookieHeader = String.format("refreshToken=; Path=/; HttpOnly; Expires=Thu, 01 Jan 1970 00:00:00 GMT; %s; SameSite=Lax",
                secureCookie ? "Secure" : "");

        response.addHeader("Set-Cookie", cookieHeader);
        log.debug("클라이언트의 쿠키에서 리프레시 토큰 삭제 완료: {}", cookieHeader);
    }
}