package com.USWCicrcleLink.server.global.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 잘못된 JWT 토큰 예외 (변조된 토큰, 서명 불일치 등)
 */
public class CustomAuthenticationException extends AuthenticationException {
    public CustomAuthenticationException(String message) {
        super(message);
    }
}