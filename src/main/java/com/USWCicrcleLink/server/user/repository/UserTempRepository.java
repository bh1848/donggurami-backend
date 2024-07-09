package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.UserTemp;
import org.springframework.data.jpa.repository.JpaRepository;

// 임시 회원 정보를 저장하는 리포지토리
public interface UserTempRepository extends JpaRepository<UserTemp, Long> {

    // 이메일로 회원 조회
    UserTemp findByTempEmail(String tempEmail);
}

