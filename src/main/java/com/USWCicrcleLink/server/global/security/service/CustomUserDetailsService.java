package com.USWCicrcleLink.server.global.security.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.util.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.util.CustomUserDetails;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {
        return loadUserByUuidAndRole(uuid, null);
    }

    public UserDetails loadUserByUuidAndRole(String uuid, Role role) throws UsernameNotFoundException {
        UUID userUuid;
        try {
            userUuid = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("유효하지 않은 UUID 형식입니다: " + uuid, e);
        }

        if (role == null || role == Role.ADMIN) {
            Admin admin = adminRepository.findByAdminUUID(userUuid).orElse(null);
            if (admin != null) {
                return new CustomAdminDetails(admin);
            }
        }

        if (role == null || role == Role.USER) {
            User user = userRepository.findByUserUUID(userUuid).orElse(null);
            if (user != null) {
                return new CustomUserDetails(user);
            }
        }

        throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + uuid);
    }
}