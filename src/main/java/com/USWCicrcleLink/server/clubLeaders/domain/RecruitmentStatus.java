package com.USWCicrcleLink.server.clubLeaders.domain;

public enum RecruitmentStatus {
    OPEN,
    CLOSED;

    public boolean isOpen() {
        return this == OPEN;
    }
}
