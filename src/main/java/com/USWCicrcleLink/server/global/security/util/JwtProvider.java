package com.USWCicrcleLink.server.global.security.util;

import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.service.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

/**
 * JWT를 생성하고 검증
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_HEADER = "Refresh";
    public static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1800000L;
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 604800000L;

    private final CustomUserDetailsService customUserDetailsService;

    @Value("${jwt.secret.key}")
    private String secretKeyString;
    private Key secretKey;
    
    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        log.debug("JWT 비밀 키 초기화 완료");
    }

    // 엑세스 토큰 생성
    public String createAccessToken(String uuid, Role role, List<Long> clubIds) {
        Claims claims = Jwts.claims().setSubject(uuid);
        claims.put("role", role.name());
        claims.put("clubIds", clubIds);
        Date now = new Date();
        String token = buildToken(claims, now, ACCESS_TOKEN_EXPIRATION_TIME);
        log.debug("엑세스 토큰 생성: {}", token);
        return token;
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(String uuid) {
        Claims claims = Jwts.claims().setSubject(uuid);
        Date now = new Date();
        String token = buildToken(claims, now, REFRESH_TOKEN_EXPIRATION_TIME);
        log.debug("리프레시 토큰 생성: {}", token);
        return token;
    }

    // JWT 빌드
    private String buildToken(Claims claims, Date now, long expirationTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 엑세스 토큰에서 UUID 추출
    public String getUUID(String accessToken) {
        String uuid = getClaims(accessToken).getSubject();
        log.debug("액세스 토큰에서 추출한 UUID: {}", uuid);
        return uuid;
    }

    // 엑세스 토큰에서 role 추출
    public Role getRole(String accessToken) {
        Claims claims = getClaims(accessToken);
        Role role = Role.valueOf(claims.get("role").toString());
        log.debug("액세스 토큰에서 추출한 역할: {}", role);
        return role;
    }

    // 엑세스 토큰에서 사용자 정보 추출
    public UserDetails getUserDetails(String accessToken) {
        String uuid = getUUID(accessToken);
        Role role = getRole(accessToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUuidAndRole(uuid, role);
        log.debug("액세스 토큰에서 추출한 사용자 세부 정보: {}", userDetails);
        return userDetails;
    }

   // 엑세스 토큰 유효성 검증
    public boolean validateAccessToken(String accessToken) {
        try {
            Jws<Claims> claims = getClaimsJws(accessToken);
            boolean isValid = !claims.getBody().getExpiration().before(new Date());
            log.debug("액세스 토큰 유효성 검증 결과: {}", isValid);
            return isValid;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 검증 실패: {}", e.getMessage(), e);
            return false;
        }
    }

   // JWT에서 클레임 추출
    private Jws<Claims> getClaimsJws(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
    }

    private Claims getClaims(String token) {
        return getClaimsJws(token).getBody();
    }

    // 헤더에서 엑세스 토큰 추출
    public String resolveAccessToken(HttpServletRequest request) {
        return resolveToken(request, AUTHORIZATION_HEADER);
    }

   // 헤더에서 리프레시 토큰 추출
    public String resolveRefreshToken(HttpServletRequest request) {
        return resolveToken(request, REFRESH_HEADER);
    }

   // 헤더에서 JWT 추출
    private String resolveToken(HttpServletRequest request, String header) {
        String bearerToken = request.getHeader(header);
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
}
