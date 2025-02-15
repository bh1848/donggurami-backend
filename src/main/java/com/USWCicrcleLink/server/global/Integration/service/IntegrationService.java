package com.USWCicrcleLink.server.global.Integration.service;

import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IntegrationService {

    private final JwtProvider jwtProvider;
    private final ProfileRepository profileRepository;

    // 동아리 회장, 동연회-개발자, 사용자 통합 로그아웃
    public void integrationLogout(HttpServletRequest request, HttpServletResponse response) {

        // 리프레시 토큰 추출
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        if (refreshToken != null && jwtProvider.validateRefreshToken(refreshToken)) {
            // 유효한 리프레시 토큰인 경우, UUID 추출 및 모든 리프레시 토큰 삭제
            String uuid = jwtProvider.getUUIDFromRefreshToken(refreshToken);
            jwtProvider.deleteRefreshTokensByUuid(uuid);
            log.debug("로그아웃: 사용자 {}의 모든 리프레시 토큰 삭제 완료", uuid);

            // 모바일 사용자의 fcm 토큰 삭제
            Optional<Profile> userFcmToken = profileRepository.findByUser_UserUUID(UUID.fromString(uuid));
            if (userFcmToken.map(Profile::getFcmToken).isPresent()) {
                userFcmToken.get().updateFcmToken(null);
                profileRepository.save(userFcmToken.get());
                log.debug("로그아웃: 모바일 사용자 {}의 fcm 토큰 삭제 완료", uuid);
            }
        } else {
            log.debug("리프레시 토큰이 존재하지 않거나 유효하지 않음. 로그아웃 처리 계속 진행.");
        }

        // 클라이언트의 쿠키에서 리프레시 토큰 삭제
        jwtProvider.deleteRefreshTokenCookie(response);

        log.debug("로그아웃 성공");
    }
}