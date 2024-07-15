package com.USWCicrcleLink.server.admin;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AdminDataInitializer {

    private final AdminRepository adminRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (adminRepository.findAll().isEmpty()) {
                Admin admin = Admin.builder()
                        .adminAccount("admin")
                        .adminPw("1234")
                        .adminName("동아리 연합회")
                        .build();
                adminRepository.save(admin);
            }
        };
    }
}
