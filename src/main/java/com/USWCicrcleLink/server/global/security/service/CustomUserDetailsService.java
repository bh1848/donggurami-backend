package com.USWCicrcleLink.server.global.security.service;

import com.USWCicrcleLink.server.global.security.util.CustomUserDetails;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UUID userUUID;
        try {
            userUUID = UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("유효하지 않은 UUID 형식입니다: " + username, e);
        }

        User user = userRepository.findByUserUUID(userUUID).orElseThrow(
                () -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username)
        );
        return new CustomUserDetails(user);
    }
}
