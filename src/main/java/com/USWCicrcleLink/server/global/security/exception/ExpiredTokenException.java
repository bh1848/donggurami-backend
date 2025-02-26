package com.USWCicrcleLink.server.global.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 만료된 JWT 토큰 예외
 */
public class ExpiredTokenException extends AuthenticationException {
    public ExpiredTokenException(String message) {
        super(message);
    }
}
