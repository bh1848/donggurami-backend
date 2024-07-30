package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.user.domain.AuthToken;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.repository.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final AuthTokenRepository authTokenRepository;
    public void createAndSaveAuthToken(User user, String authNumber) {
        AuthToken authToken = AuthToken.createAuthToken(user, authNumber);
        authTokenRepository.save(authToken);
    }

    // 인증 완료된 토큰 삭제
    @Transactional
    public void deleteAuthToken(UUID uuid){
        Optional<AuthToken> authToken = authTokenRepository.findByUserUserUUID(uuid);
        authTokenRepository.delete(authToken.get());
    }

}