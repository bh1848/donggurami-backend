package com.USWCicrcleLink.server.global.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * JWT 인증 예외
 */
public class CustomAuthenticationException extends AuthenticationException {
    public CustomAuthenticationException(String message) {
        super(message, null);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
