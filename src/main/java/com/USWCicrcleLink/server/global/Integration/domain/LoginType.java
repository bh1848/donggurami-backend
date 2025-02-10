package com.USWCicrcleLink.server.global.Integration.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum LoginType {

    ADMIN("admin"),

    LEADER("leader");

    private final String value;

    LoginType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static LoginType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("잘못된 로그인 타입입니다.");
        }

        return Stream.of(LoginType.values())
                .filter(type -> type.value.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 로그인 타입입니다."));
    }
}
