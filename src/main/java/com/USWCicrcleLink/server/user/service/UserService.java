package com.USWCicrcleLink.server.user.service;


import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailService;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import com.USWCicrcleLink.server.user.repository.UserTempRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository ;
    private final UserTempRepository userTempRepository;
    private final EmailService emailService;

    public void updatePW(UUID uuid, String newPassword, String confirmNewPassword){

        User user = userRepository.findByUserUUID(uuid);
        if (user == null) {
            throw new IllegalArgumentException("해당 UUID를 가진 사용자를 찾을 수 없습니다: " + uuid);
        }
        if (!confirmNewPassword.equals(user.getUserPw())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        user.setUserPw(newPassword);
        userRepository.save(user);
    }

    // 임시 회원 가입
    public UserTemp signUpUserTemp(SignUpRequest request){

        UserTemp userTemp = request.toEntity();
        userTempRepository.save(userTemp);

        return userTempRepository.findByTempEmail(userTemp.getTempEmail());
    }


    // 이메일 전송
    public UUID sendVerifyEmail(UserTemp userTemp) throws MessagingException {
        // 이메일 토큰 생성
        EmailToken emailToken = emailService.createmailToken(userTemp);
        // 이메일 전송
        MimeMessage message = emailService.createVerifyLink(userTemp, emailToken);
        emailService.sendEmail(message);

        return emailToken.getEmailTokenId();
    }


}