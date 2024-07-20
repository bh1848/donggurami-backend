package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailService;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
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
import java.time.LocalDateTime;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository ;
    private final UserTempRepository userTempRepository;
    private final EmailService emailService;
    private final ProfileRepository profileRepository;

    public void updatePW(UUID uuid, String newPassword, String confirmNewPassword) {

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

    public UserTemp signUpUserTemp(SignUpRequest request){

        userTempRepository.save(request.toEntity());
        return userTempRepository.findByTempEmail(request.getEmail());
    }

    @Transactional
    public UUID sendEmail(UserTemp userTemp) throws MessagingException {

        EmailToken emailToken = emailService.createmailToken(userTemp);
        MimeMessage message = emailService.createAuthLink(userTemp, emailToken);

        emailService.sendEmail(message);

        return emailToken.getEmailTokenId();
    }

    public UserTemp checkEmailToken(UUID emailTokenId){

        // 토큰 검증
        emailService.checkEmailToken(emailTokenId);
        EmailToken token = emailService.getTokenBy(emailTokenId);

        return token.getUserTemp();
    }

    // 회원가입
    @Transactional
    public User signUpUser(UserTemp userTemp) {

        //User 객체 생성 및 저장
        User user = User.builder()
                .userUUID(UUID.randomUUID())
                .userAccount(userTemp.getTempAccount())
                .userPw(userTemp.getTempPw())
                .email(userTemp.getTempEmail())
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .build();

        //Profile 객체 생성 및 저장
        Profile profile = Profile.builder()
                .user(user)
                .userName(userTemp.getTempName())
                .studentNumber(userTemp.getTempStudentNumber())
                .userHp(userTemp.getTempHp())
                .major(userTemp.getTempMajor())
                .profileCreatedAt(LocalDateTime.now())
                .profileUpdatedAt(LocalDateTime.now())
                .build();


        // 회원 가입
        userRepository.save(user);
        profileRepository.save(profile);
        // 임시 회원 정보 삭제
        emailService.deleteTokenBy(userTemp);
        return user;
    }
}