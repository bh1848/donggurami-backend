package com.USWCicrcleLink.server.email.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.repository.EmailTokenRepository;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTokenService {

    private  final EmailTokenRepository emailTokenRepository;

    // 토큰 생성
    @Transactional
    public EmailToken createEmailToken(UserTemp userTemp) {
        EmailToken emailToken = EmailToken.createEmailToken(userTemp);
        return emailTokenRepository.save(emailToken);
    }

    // 유효한 토큰 검증
    public void verifyEmailToken (UUID emailTokenId) {

        EmailToken emailToken = emailTokenRepository.findByEmailTokenId(emailTokenId)
                .orElseThrow(() -> new NoSuchElementException("해당 emailTokenId 를 가진 회원이 없습니다"));
        try {
            emailToken.verifyExpireTime();
        } finally {
            emailTokenRepository.save(emailToken);
        }
    }

    // 임시 회원 정보 삭제
    @Transactional
    public void deleteEmailTokenAndUserTemp(UserTemp userTemp){
        EmailToken findToken = emailTokenRepository.findByUserTemp(userTemp);
        emailTokenRepository.delete(findToken);
    }

    public EmailToken getEmailToken(UUID emailTokenId){
        return emailTokenRepository.findByEmailTokenId(emailTokenId)
                .orElseThrow(() -> new NoSuchElementException("해당 emailTokenId 를 가진 회원이 없습니다"));
    }

    // 이메일 인증 토큰 업데이트
   public EmailToken updateCertificationTime (UUID emailTokenId) {

       EmailToken findToken = getEmailToken(emailTokenId);

       // 이메일 토큰 필드 갱신
       findToken.updateExpiredToken();
       emailTokenRepository.save(findToken);

        return findToken;
    }
}
