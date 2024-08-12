package com.USWCicrcleLink.server.global.login.domain;

import java.util.UUID;

public interface IntegratedUser {
    String getIntegratedAccount();
    String getIntegratedPw();
    UUID getIntegratedUUID();
}
