package com.USWCicrcleLink.server.clubLeader.service;

import com.USWCicrcleLink.server.clubLeader.dto.FcmMessageDto;
import com.USWCicrcleLink.server.clubLeader.dto.FcmSendDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class FcmServiceImpl implements FcmService {

    private final String FMC_API_URL = "https://fcm.googleapis.com/v1/projects/usw-circle-link/messages:send";
    private final String firebaseConfigPath = "firebase/usw-circle-link-firebase-adminsdk-u25m3-791f80d22c.json";
    private final String GOOGLE_AUTH_URL = "https://www.googleapis.com/auth/cloud-platform";

    // 메시지 구성, 토큰 받고 메시지 처리
    @Override
    public int sendMessageTo(FcmSendDto fcmSendDto) throws IOException {
        // 메시지 구성
        String message = makeMessage(fcmSendDto);

        RestTemplate restTemplate = new RestTemplate();
        // 한글 설정
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", " Bearer " + getAccessToken());

        HttpEntity entity = new HttpEntity<>(message, headers);
        ResponseEntity response = restTemplate.exchange(FMC_API_URL, HttpMethod.POST, entity, String.class);

        log.info(response.getStatusCode().toString());
        log.info("푸시 알림 전송 완료");
        return response.getStatusCode() == HttpStatus.OK ? 1 : 0;
    }

    // Firebase Admin SDK의 비공개 키 참조해 bearer 토큰 발급
    private String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of(GOOGLE_AUTH_URL));

        // 토큰 만료 ? 갱신 : 토큰
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    // FCM 전송 정보를 기반으로 메시지 구성
    private String makeMessage(FcmSendDto fcmSendDto) throws JsonProcessingException {
        // json변환
        ObjectMapper om = new ObjectMapper();

        // 메시지 구성
        FcmMessageDto fcmMessageDto = FcmMessageDto.builder()
                .message(FcmMessageDto.Message.builder()
                        .token(fcmSendDto.getToken())
                        .notification(FcmMessageDto.Notification.builder()
                                .title(fcmSendDto.getTitle())
                                .body(fcmSendDto.getBody())
                                .image(null)
                                .build()
                        ).build()).validateOnly(false).build();

        return om.writeValueAsString(fcmMessageDto);
    }
}
