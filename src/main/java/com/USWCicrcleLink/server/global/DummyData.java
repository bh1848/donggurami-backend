package com.USWCicrcleLink.server.global;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.global.security.domain.Role;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Transactional
@RequiredArgsConstructor
public class DummyData {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    @PostConstruct
    public void init() {
        initAdmin();
    }

    //관리자 동연회 데이터
    public void initAdmin() {
        // 동아리 연합회 관리자 계정
        Admin adminUnion = Admin.builder()
                .adminUUID(UUID.randomUUID())
                .adminAccount("clubUnion")
                .adminPw(passwordEncoder.encode("hpsEetcTf7ymgy6"))  // 비밀번호 암호화
                .adminName("동아리 연합회 관리자")
                .role(Role.ADMIN)
                .build();

        // 개발자 계정
        Admin developer = Admin.builder()
                .adminUUID(UUID.randomUUID())
                .adminAccount("developer")
                .adminPw(passwordEncoder.encode("5MYcg7Cuvrh50fS"))  // 비밀번호 암호화
                .adminName("개발자")
                .role(Role.ADMIN)
                .build();

        // 데이터 저장
        adminRepository.save(adminUnion);
        adminRepository.save(developer);
    }
}
