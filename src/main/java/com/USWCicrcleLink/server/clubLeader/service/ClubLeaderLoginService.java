package com.USWCicrcleLink.server.clubLeader.service;


import com.USWCicrcleLink.server.clubLeader.dto.LeaderLoginRequest;
import com.USWCicrcleLink.server.clubLeader.dto.LeaderLoginResponse;
import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.details.service.CustomUserDetailsService;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
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
public class ClubLeaderLoginService {

    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtProvider jwtProvider;

    /**
     * 로그인 (Leader)
     */
    @RateLimite(action = "WEB_LOGIN")
    public LeaderLoginResponse leaderLogin(LeaderLoginRequest request, HttpServletResponse response) {
        UserDetails userDetails = customUserDetailsService.loadUserByAccountAndRole(request.getLeaderAccount(), Role.LEADER);

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

        log.debug("Leader 로그인 성공 - 계정: {}", request.getLeaderAccount());

        String accessToken = jwtProvider.createAccessToken(leaderUUID, response);
        String refreshToken = jwtProvider.createRefreshToken(leaderUUID, response);

        return new LeaderLoginResponse(accessToken, refreshToken, Role.LEADER, clubUUID, isAgreedTerms);
    }

    // Leader UUID 추출 (CustomLeaderDetails에서 가져옴)
    private UUID extractLeaderUUID(UserDetails userDetails) {
        if (userDetails instanceof CustomLeaderDetails customLeaderDetails) {
            return customLeaderDetails.leader().getLeaderUUID();
        }
        throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
    }
}