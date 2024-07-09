package com.USWCicrcleLink.server.user.open.service;

import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import com.USWCicrcleLink.server.user.repository.UserTempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserOpenService {

    private final UserTempRepository userTempRepository;

    // 임시 회원 가입
    public UserTemp signUpMemberTemp(SignUpRequest request){

        UserTemp userTemp = request.toEntity();

        userTempRepository.save(userTemp);
        log.info("임시 회원가입 완료: {}", userTemp.getTempAccount());

       return userTempRepository.findByTempEmail(userTemp.getTempEmail());

    }





}
