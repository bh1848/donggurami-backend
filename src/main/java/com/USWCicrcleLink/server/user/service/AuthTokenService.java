package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.user.domain.AuthToken;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.UserInfoDto;
import com.USWCicrcleLink.server.user.repository.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthTokenService {

    private final AuthTokenRepository authTokenRepository;

    // 인증 코드 토큰 생성
    @Transactional
    public  AuthToken createAuthToken(User user) {
        AuthToken authToken = AuthToken.createAuthToken(user);
        return authTokenRepository.save(authToken);
    }

    // 인증 코드 토큰 검증
    public void verifyAuthToken(UUID uuid, UserInfoDto request) {

        log.info("인증 코드 토큰 검증 메서드 시작");
        AuthToken authToken = authTokenRepository.findByUserUserUUID(uuid)
                .orElseThrow(()-> new UserException(ExceptionType.USER_UUID_NOT_FOUND));

        log.info("uuid ={} 에 해당하는 회원 조회 완료", uuid);
        log.info("인증 코드 일치 확인 시작");
        if (!authToken.isAuthCodeValid(request.getAuthCode())) {
            throw new UserException(ExceptionType.INVALID_AUTH_CODE);
        }
        log.info("인증 코드 토큰 검증 완료");
    }

    // 검증 완료된 인증 코드 토큰 삭제
    @Transactional
    public void deleteAuthToken(UUID uuid){

        AuthToken authToken = authTokenRepository.findByUserUserUUID(uuid)
                .orElseThrow(()-> new UserException(ExceptionType.USER_UUID_NOT_FOUND));

        authTokenRepository.delete(authToken);
        log.info("검증 완료된 인증 코드 토큰 삭제 완료");
    }



}
