package com.USWCicrcleLink.server.club.domain;

public enum RecruitmentStatus {
    OPEN,
    CLOSED;

    public boolean isOpen() {
        return this == OPEN;
    }
}
