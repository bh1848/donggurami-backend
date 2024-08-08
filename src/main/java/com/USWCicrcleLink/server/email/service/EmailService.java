package com.USWCicrcleLink.server.email.service;

import com.USWCicrcleLink.server.email.config.EmailConfig;
import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.EmailException;
import com.USWCicrcleLink.server.user.domain.AuthToken;
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


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final EmailConfig emailConfig;
    private final JavaMailSender javaMailSender;

    //이메일 인증 경로
    private static final String VERIFY_EMAIL_PATH = "/users/email/verify-token";


    @Async
    public void sendEmail(MimeMessage mimeMessage) {
        javaMailSender.send(mimeMessage);
    }

    // 회원가입 링크 생성
    public MimeMessage createSingUpLink(UserTemp userTemp,EmailToken emailToken)  {

       try {
           // 회원 가입 인증 메일 생성
           MimeMessage mimeMessage = javaMailSender.createMimeMessage();
           MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
           helper.setTo(userTemp.getTempEmail() + "@suwon.ac.kr");
           helper.setSubject("회원가입 이메일 인증");
           helper.setFrom("wg1004s@naver.com");

           String emailContent
                   = "<a href='" + emailConfig.getBaseUrl() + VERIFY_EMAIL_PATH + "?emailTokenId=" + emailToken.getEmailTokenId() + "'> 이메일 확인 </a>";
           helper.setText(emailContent, true);

           return mimeMessage;

       } catch (MessagingException e){
           e.printStackTrace();
           throw new EmailException(ExceptionType.SEND_MAIL_FAILED);
       }
    }


   // 아이디 찾기 메일 생성
    public  MimeMessage createAccountInfoMail (User findUser)  {

        try{
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
            helper.setTo(findUser.getEmail() + "@suwon.ac.kr");
            helper.setSubject("동구라미의 아이디를 찾기 위한 메일입니다.");
            helper.setText("회원님의 아이디는  "  + findUser.getUserAccount() + " 입니다.");
            helper.setFrom("wg1004s@naver.com");

            return mimeMessage;

        } catch (MessagingException e){
            e.printStackTrace();
            throw new EmailException(ExceptionType.SEND_MAIL_FAILED);
        }

    }

    // 인증 코드 메일 생성
    public  MimeMessage createAuthCodeMail(User user,AuthToken authToken)  {

        try{
            // 메세지 생성
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
            helper.setTo(user.getEmail() + "@suwon.ac.kr");
            helper.setSubject("비밀번호 찾기 메일 입니다.");
            helper.setText("인증코드는  "  + authToken.getAuthCode()+ " 입니다.");
            helper.setFrom("wg1004s@naver.com");

            return mimeMessage;

        } catch (MessagingException e){
            e.printStackTrace();
            throw new EmailException(ExceptionType.SEND_MAIL_FAILED);
        }

    }


}




