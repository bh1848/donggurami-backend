package com.USWCicrcleLink.server.clubLeader.service;

import com.USWCicrcleLink.server.clubLeader.dto.LeaderLoginRequest;
import com.USWCicrcleLink.server.clubLeader.dto.LeaderLoginResponse;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.security.details.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubLeaderAuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * 로그인 (Leader)
     */
    public LeaderLoginResponse leaderLogin(LeaderLoginRequest request, HttpServletResponse response) {
        UserDetails userDetails = loadLeaderDetails(request.getLeaderAccount());

        if (!passwordEncoder.matches(request.getLeaderPw(), userDetails.getPassword())) {
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }

        // Leader UUID 및 동의 여부 추출
        UUID leaderUUID = extractLeaderUUID(userDetails);
        UUID clubUUID = null;
        Boolean isAgreedTerms = false;

        if (userDetails instanceof CustomLeaderDetails leaderDetails) {
            clubUUID = leaderDetails.getClubUUID();
            isAgreedTerms = leaderDetails.getIsAgreedTerms();
        }

        // UUID 기반 JWT 생성
        String accessToken = jwtProvider.createAccessToken(leaderUUID, response);
        String refreshToken = jwtProvider.createRefreshToken(leaderUUID, response, Role.LEADER);

        log.debug("로그인 성공, uuid: {}", leaderUUID);
        return new LeaderLoginResponse(accessToken, refreshToken, Role.LEADER, clubUUID, isAgreedTerms);
    }

    // Leader UUID 추출 (CustomLeaderDetails에서 가져옴)
    private UUID extractLeaderUUID(UserDetails userDetails) {
        if (userDetails instanceof CustomLeaderDetails customLeaderDetails) {
            return customLeaderDetails.leader().getLeaderUUID();
        }
        throw new UserException(ExceptionType.USER_NOT_EXISTS);
    }

    // account 및 role 확인
    private UserDetails loadLeaderDetails(String account) {
        try {
            return customUserDetailsService.loadUserByAccountAndRole(account, Role.LEADER);
        } catch (UserException e) {
            log.warn("동아리 회장 로그인 실패 - 존재하지 않는 계정: {}", account);
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }
    }

    /**
     * 로그아웃 (Leader)
     */
    public void leaderLogout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        if (refreshToken != null && jwtProvider.validateRefreshToken(refreshToken, Role.LEADER)) {
            UUID uuid = jwtProvider.getUUIDFromRefreshToken(refreshToken, Role.LEADER);

            // 로그아웃 시 리프레시 토큰 블랙리스트 적용 후 삭제
            jwtProvider.blacklistRefreshToken(refreshToken, Role.LEADER);
            jwtProvider.deleteRefreshTokensByUuid(uuid);
            log.debug("LEADER 로그아웃 - UUID: {}", uuid);
        } else {
            log.debug("유효하지 않은 리프레시 토큰 - 로그아웃 계속 진행");
        }

        // 클라이언트 쿠키에서 리프레시 토큰 삭제
        jwtProvider.deleteRefreshTokenCookie(response);
        log.info("LEADER 로그아웃 성공");
    }
}