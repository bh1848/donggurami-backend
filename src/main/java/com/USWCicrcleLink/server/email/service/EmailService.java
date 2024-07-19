package com.USWCicrcleLink.server.email.service;
import com.USWCicrcleLink.server.email.config.EmailConfig;
import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.repository.EmailTokenRepository;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final EmailConfig emailConfig;
    private final EmailTokenRepository emailTokenRepository;
    private final JavaMailSender javaMailSender;

    //이메일 인증 경로
    private static final String CONFIRM_EMAIL_PATH = "/user/verify-email";



    // 인증 링크 생성
    public MimeMessage createAuthLink(UserTemp userTemp,EmailToken token) throws MessagingException {

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(userTemp.getTempEmail() + "@suwon.ac.kr");
        helper.setSubject("회원가입 이메일 인증");
        helper.setFrom("wg1004s@naver.com");

        String emailContent
                = "<a href='" + emailConfig.getBaseUrl() + CONFIRM_EMAIL_PATH + "?emailTokenId=" +token.getEmailTokenId() + "'> verify-email </a>";
        helper.setText(emailContent, true);

        return message;
    }

    @Async
    public void sendEmail(MimeMessage mimeMessage) {
        javaMailSender.send(mimeMessage);
    }

    @Transactional
    public  EmailToken createmailToken(UserTemp userTemp) {
        EmailToken emailToken = EmailToken.createEmailToken(userTemp);
        return emailTokenRepository.save(emailToken);
    }


    // 유효한 토큰 검증
    public void checkEmailToken(UUID emailTokenId) {

        // emailToken 이 존재하는지 검사
        EmailToken emailToken = emailTokenRepository.findByEmailTokenId(emailTokenId)
                .orElseThrow(() -> new NoSuchElementException("해당 emailTokenId 를 가진 회원이 없습니다"));

        // 해당 토큰의 만료 시간 검사 및 처리
        try {
            emailToken.validateAndExpire();
        } finally {
            emailTokenRepository.save(emailToken); // 상태 변경 후 저장
        }

    }

    public void deleteTokenBy(UserTemp userTemp){
        EmailToken findToken = emailTokenRepository.findByUserTemp(userTemp);
        emailTokenRepository.delete(findToken);
    }

    public EmailToken getTokenBy(UUID id){
        return emailTokenRepository.findByEmailTokenId(id)
                .orElseThrow(() -> new NoSuchElementException("해당 emailTokenId 를 가진 회원이 없습니다"));
    }

}




