package com.USWCicrcleLink.server.email.service;

import com.USWCicrcleLink.server.email.config.EmailConfig;
import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.repository.EmailTokenRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Random;
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
    private String authNumber; //  인증 코드

    @Async
    public void sendEmail(MimeMessage mimeMessage) {
        javaMailSender.send(mimeMessage);
    }

    // 인증 링크 생성
    public MimeMessage createAuthLink(UserTemp userTemp) throws MessagingException {

        // 이메일 토큰 조회
        EmailToken token = emailTokenRepository.findByUserTemp(userTemp);

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(userTemp.getTempEmail() + "@suwon.ac.kr");
        helper.setSubject("회원가입 이메일 인증");
        helper.setFrom("wg1004s@naver.com");

        String emailContent
                = "<a href='" + emailConfig.getBaseUrl() + CONFIRM_EMAIL_PATH + "?emailTokenId=" +token.getEmailTokenId()+ "'> verify-email </a>";
        helper.setText(emailContent, true);

        return mimeMessage;
    }


    public void createEmailToken(UserTemp userTemp) {
        EmailToken emailToken = EmailToken.createEmailToken(userTemp);
        emailTokenRepository.save(emailToken);
    }

    // 유효한 토큰 검증
    public void validateToken (UUID emailTokenId) {

        EmailToken emailToken = emailTokenRepository.findByEmailTokenId(emailTokenId)
                .orElseThrow(() -> new NoSuchElementException("해당 emailTokenId 를 가진 회원이 없습니다"));
        try {
            emailToken.validateAndExpire();
        } finally {
            emailTokenRepository.save(emailToken);
        }
    }

    public void sendEmailInfo(User findUser) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
        helper.setTo(findUser.getEmail() + "@suwon.ac.kr");
        helper.setSubject("동구라미의 아이디를 찾기 위한 메일입니다.");
        helper.setText("회원님의 아이디는  "  + findUser.getUserAccount() + " 입니다.");
        helper.setFrom("wg1004s@naver.com");

        javaMailSender.send(mimeMessage);
    }

    public void deleteTempUserAndToken(UserTemp userTemp){
        EmailToken findToken = emailTokenRepository.findByUserTemp(userTemp);
        emailTokenRepository.delete(findToken);
    }

    public EmailToken getTokenBy(UUID emailTokenId){
        return emailTokenRepository.findByEmailTokenId(emailTokenId)
                .orElseThrow(() -> new NoSuchElementException("해당 emailTokenId 를 가진 회원이 없습니다"));
    }


    public void sendAuthCode(String email) throws MessagingException {

        makeRandomNumber();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
        helper.setTo(email + "@suwon.ac.kr");
        helper.setSubject("비밀번호 찾기 메일 입니다.");
        helper.setText("인증코드는  "  + authNumber+ " 입니다.");
        helper.setFrom("wg1004s@naver.com");

        javaMailSender.send(mimeMessage);
    }

    private void  makeRandomNumber() {
            Random r = new Random();
            StringBuilder randomNumber = new StringBuilder();
            for(int i = 0; i < 4; i++) {
                randomNumber.append(r.nextInt(10));
            }
            authNumber= randomNumber.toString();
    }



}




