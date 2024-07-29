package com.USWCicrcleLink.server.clubLeader.service;

import com.USWCicrcleLink.server.clubLeader.dto.FcmSendDto;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface FcmService {
    int sendMessageTo(FcmSendDto fcmSendDto) throws IOException;
}
