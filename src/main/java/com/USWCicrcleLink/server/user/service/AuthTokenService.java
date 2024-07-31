package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.user.domain.AuthToken;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.UserInfoDto;
import com.USWCicrcleLink.server.user.repository.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final AuthTokenRepository authTokenRepository;

    @Transactional
    public void createAndSaveAuthToken(User user, String authNumber) {
        AuthToken authToken = AuthToken.createAuthToken(user, authNumber);
        authTokenRepository.save(authToken);
    }

    // 인증 코드 토큰 검증
    public void validateAuthToken(UUID uuid, UserInfoDto request) {

        AuthToken authToken = authTokenRepository.findByUserUserUUID(uuid)
                .orElseThrow(() -> new NoSuchElementException("uuid 값이 올바르지 않습니다"));

        if (!authToken.isAuthCodeValid(request.getAuthCode())) {
            throw new IllegalArgumentException("인증코드가 일치 하지않습니다");
        }
    }

    // 인증 완료된 토큰 삭제
    @Transactional
    public void deleteAuthToken(UUID uuid){

        AuthToken authToken = authTokenRepository.findByUserUserUUID(uuid)
                .orElseThrow(() -> new NoSuchElementException("uuid 값이 올바르지 않습니다"));

        authTokenRepository.delete(authToken);
    }



}
