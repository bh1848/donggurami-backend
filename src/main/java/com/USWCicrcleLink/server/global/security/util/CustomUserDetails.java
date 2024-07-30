package com.USWCicrcleLink.server.global.security.util;

import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.user.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role role = user.getRoles();
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return user.getUserPw();
    }

    @Override
    public String getUsername() {
        return user.getUserUUID().toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; //계정이 만료되지 않았는지 여부를 반환합니다.
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; //계정이 잠기지 않았는지 여부를 반환합니다.
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; //자격 증명이 만료되지 않았는지 여부를 반환합니다.
    }

    @Override
    public boolean isEnabled() {
        return true; //계정이 활성화되었는지 여부를 반환합니다.
    }
}
