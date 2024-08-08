package com.USWCicrcleLink.server.global.login.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.global.login.domain.IntegratedUser;
import com.USWCicrcleLink.server.global.login.domain.LoginType;
import com.USWCicrcleLink.server.global.login.dto.IntegratedLoginRequest;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.login.dto.IntegratedLoginResponse;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.dto.TokenDto;
import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IntegratedLoginService {    // 동아리 회장, 동연회-개발자 통합 로그인

    private final JwtProvider jwtProvider;
    private final AdminRepository adminRepository;
    private final LeaderRepository leaderRepository;

    public IntegratedLoginResponse integratedLogin(IntegratedLoginRequest loginRequest) {
        log.info("로그인 요청: {}, 사용자 유형: {}", loginRequest.getIntegratedAccount(), loginRequest.getLoginType());

        IntegratedUser user;
        Role role;

        if (loginRequest.getLoginType() == LoginType.ADMIN) {
            user = adminRepository.findByAdminAccount(loginRequest.getIntegratedAccount())
                    .orElseThrow(() -> new RuntimeException("아이디나 비밀번호를 확인해주세요."));
            role = Role.ADMIN;
        } else if (loginRequest.getLoginType() == LoginType.LEADER) {
            user = leaderRepository.findByLeaderAccount(loginRequest.getIntegratedAccount())
                    .orElseThrow(() -> new RuntimeException("아이디나 비밀번호를 확인해주세요."));
            role = Role.LEADER;
        } else {
            throw new RuntimeException("잘못된 사용자 유형입니다.");
        }

        if (!user.getIntegratedPw().equals(loginRequest.getIntegratedPw())) {
            throw new RuntimeException("아이디나 비밀번호를 확인해주세요.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getIntegratedUUID().toString());
        return new IntegratedLoginResponse(accessToken, role);
    }
}
