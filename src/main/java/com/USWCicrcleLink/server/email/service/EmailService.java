package com.USWCicrcleLink.server.email.service;

import com.USWCicrcleLink.server.email.config.EmailConfig;
import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.repository.EmailTokenRepository;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.repository.UserTempRepository;
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
@Transactional
public class EmailService {

    private final EmailConfig emailConfig;
    private final EmailTokenRepository emailTokenRepository;
    private final JavaMailSender javaMailSender;

    //이메일 인증 경로
    private static final String CONFIRM_EMAIL_PATH = "/user/confirm-email";
    // 이메일 토큰 만료 시간
    private static final long EMAIL_TOKEN_CERTIFICATION_TIME_VALUE = 5L;


    // 인증 링크 생성
    public MimeMessage createVerifyLink(UserTemp userTemp,EmailToken token) throws MessagingException {
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
        return emailTokenRepository.save(EmailToken.createEmailToken(userTemp));
    }

    // 유효한 토큰 가져오기
    @SuppressWarnings("all")
    public EmailToken verifyEmailToken(UUID emailTokenId) {

        EmailToken emailToken = emailTokenRepository.findByEmailTokenId(emailTokenId)
                .orElseThrow(() -> new NoSuchElementException("해당 emailTokenId 를 가진 회원이 없습니다"));

        if (emailToken.isEmailTokenExpired()) {
            throw new IllegalStateException("해당 이메일은 만료되었습니다. 이메일을 재인증 해주세요");
        }

        emailToken.useToken();

        return emailToken;
    }

    @Transactional
    public void deleteTokenByUserTempId(Long id){
        EmailToken findToken = emailTokenRepository.findEmailTokenByUserTemp_UserTempId(id);
        emailTokenRepository.delete(findToken);
    }






}




