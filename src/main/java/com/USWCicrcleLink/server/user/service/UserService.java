package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.repository.EmailTokenRepository;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import com.USWCicrcleLink.server.user.repository.UserTempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserTempRepository userTempRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final EmailTokenRepository emailTokenRepository;


    // 임시 회원 가입
    public UserTemp signUpUserTemp(SignUpRequest request){

        UserTemp userTemp = request.toEntity();

        userTempRepository.save(userTemp);

       return userTempRepository.findByTempEmail(userTemp.getTempEmail());

    }

    // 임시 회원 이메일 인증 필드 갱신
    @SuppressWarnings("all")
    public void updateEmailVerification(EmailToken emailToken){
        Optional<UserTemp> userTemp = userTempRepository.findById(emailToken.getUserTempId());
        userTemp.get().emailVerifiedSuccess();
        log.info(" userAccount= {} 의 isEmailVerified= {} 로 갱신됨  ", userTemp.get().getTempAccount(), userTemp.get().isEmailVerified());
    }


    // 이메일 인증 확인 후 회원가입
    @Transactional
    @SuppressWarnings("all")
    public void signUpUser(UUID emailTokenId) {

        // emailTokenId 로 임시 회원 찾기
        Optional<EmailToken> findEmailToken = emailTokenRepository.findByEmailTokenId(emailTokenId);
        UserTemp userTemp =findEmailToken.get().getUserTemp();


        // 회원 가입
        User user=User.builder()
                .userAccount(userTemp.getTempAccount())
                .userPw(userTemp.getTempPw())
                .email(userTemp.getTempEmail())
                .build();


        Profile profile=Profile.builder()
                .userName(userTemp.getTempName())
                .studentNumber(userTemp.getTempStudentNumber())
                .userHp(userTemp.getTempHp())
                .major(userTemp.getTempMajor())
                .build();


        User savedUser = userRepository.save(user);
        profileRepository.save(profile);

        log.info("회원가입 완료: {}", user.getUserAccount());

        //임시 회원 데이터 삭제
        userTempRepository.delete(userTemp);
        log.info("임시 회원 삭제 완료: {}", userTemp.getUserTempId());


    }



}
