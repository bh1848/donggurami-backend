package com.USWCicrcleLink.server.email.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.repository.EmailTokenRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.EmailException;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new EmailException(ExceptionType.EMAIL_TOKEN_NOT_FOUND));
        try {
            emailToken.verifyExpiredTime();
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
                .orElseThrow(() -> new EmailException(ExceptionType.EMAIL_TOKEN_NOT_FOUND));
    }

    // 이메일 인증 토큰 업데이트
   public EmailToken updateCertificationTime (UUID emailTokenId) {

        // emailTokenId에 해당하는 이메일 토큰 찾기
       EmailToken findToken = getEmailToken(emailTokenId);

       // 이메일 토큰의 만료시간 갱신
       findToken.updateExpiredToken();
       emailTokenRepository.save(findToken);

        return findToken;
    }
}
