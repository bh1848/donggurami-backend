package com.USWCicrcleLink.server.global.security.util;

import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.service.CustomUserDetailsService;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider {

    // HTTP 헤더에 사용될 인증 헤더 키
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // Bearer 토큰의 접두사
    public static final String BEARER_PREFIX = "Bearer ";
    // 엑세스 토큰의 만료 시간 (30분)
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1800000L;

    // 사용자 세부 정보를 로드하는 서비스
    private final CustomUserDetailsService customUserDetailsService;

    // 비밀 키 문자열
    @Value("${jwt.secret.key}")
    private String secretKeyString;
    // JWT 서명을 위한 비밀 키 객체
    private Key secretKey;

    // 객체 초기화 후 호출되는 메서드로 비밀 키를 설정
    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    // 엑세스 토큰 생성
    public String createAccessToken(String userUUID, List<Role> roles, List<Long> clubIds) {
        Claims claims = Jwts.claims().setSubject(userUUID);
        claims.put("roles", roles.stream().map(Role::name).collect(Collectors.toList()));
        claims.put("clubIds", clubIds);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 엑세스 토큰을 HTTP 응답 헤더에 추가
    public void addAccessTokenToHeader(HttpServletResponse response, String accessToken) {
        response.addHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);
    }

    // 엑세스 토큰에서 사용자 UUID 추출
    public String getUserUUID(String accessToken) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).getBody().getSubject();
    }

    // 사용자 UUID를 이용해서 사용자 세부 정보 로드
    public UserDetails getUserDetails(String userUUID) {
        return customUserDetailsService.loadUserByUsername(userUUID);
    }

//    // 엑세스 토큰 기반으로 사용자 인증 객체 반환
//    public Authentication getAuthentication(String accessToken) {
//        UserDetails userDetails = customUserDetailsService.loadUserByUsername(getUserUUID(accessToken));
//        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
//    }

    // 엑세스 토큰 유효성 검증
    public boolean validateAccessToken(String accessToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.error("jwt 만료", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 jwt", e);
        } catch (MalformedJwtException e) {
            log.error("잘못된 jwt", e);
        }
        return false;
    }

    // 헤더에서 엑세스 토큰 추출
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
