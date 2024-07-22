package com.USWCicrcleLink.server.user.service;


import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import com.USWCicrcleLink.server.user.repository.UserTempRepository;
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

    private final UserRepository userRepository;
    private final UserTempRepository userTempRepository;

        public void updatePW(UUID uuid, String newPassword, String confirmNewPassword){

        User user = userRepository.findByUserUUID(uuid);
        if (user == null) {
            throw new IllegalArgumentException("해당 UUID를 가진 사용자를 찾을 수 없습니다: " + uuid);
        }
        if (!confirmNewPassword.equals(user.getUserPw())) {
            throw new IllegalArgumentException("기존 비밀번호와 일치하지 않습니다.");
        }

        user.updateUserPw(newPassword);
        userRepository.save(user);

        log.info("비밀번호 변경 완료: {}",user.getUserUUID());
    }

    // 임시 회원 가입
    public UserTemp signUpUserTemp(SignUpRequest request){

        UserTemp userTemp = request.toEntity();

        userTempRepository.save(userTemp);

        return userTempRepository.findByTempEmail(userTemp.getTempEmail());
        }
}