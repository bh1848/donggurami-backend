package com.USWCicrcleLink.server.clubLeader.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.clubLeader.config.FirebaseConfig;
import com.USWCicrcleLink.server.clubLeader.dto.FcmMessageDto;
import com.USWCicrcleLink.server.clubLeader.dto.FcmTokenRequest;
import com.USWCicrcleLink.server.global.security.util.CustomUserDetails;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    private final ProfileRepository profileRepository;

    private final String FCM_API_URL = "https://fcm.googleapis.com/v1/projects/usw-circle-link/messages:send";
    private final FirebaseConfig firebaseConfig;
    private final String GOOGLE_AUTH_URL = "https://www.googleapis.com/auth/cloud-platform";
    private final String APLICT_TITLE_MESSAGE = "동아리 지원 결과";
    private final String APLICT_PASS_MESSAGE = "에 합격했습니다.";
    private final String APLICT_FAIL_MESSAGE = "에 불합격했습니다.";
    private final String APLICT_ERROR_MESSAGE = "관리자에게 문의 해주세요.";

    // 메시지 구성, 토큰 받고 메시지 처리
    @Override
    public int sendMessageTo(Aplict aplict, AplictStatus aplictResult) throws IOException {
        try {
            // 메시지 구성
            String message = makeMessage(aplict, aplictResult);

            RestTemplate restTemplate = new RestTemplate();
            // 한글 설정
            restTemplate.getMessageConverters()
                    .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", " Bearer " + getAccessToken());

            HttpEntity entity = new HttpEntity<>(message, headers);
            ResponseEntity response = restTemplate.exchange(FCM_API_URL, HttpMethod.POST, entity, String.class);

            log.debug(response.getStatusCode().toString());
            log.debug("푸시 알림 전송 완료");
            return response.getStatusCode() == HttpStatus.OK ? 1 : 0;
        } catch (IOException e) {
            log.error("푸시 알림 전송 실패", e);
            return 0;
        }
    }

    // Firebase Admin SDK의 비공개 키 참조해 bearer 토큰 발급
    private String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfig.getConfigPath()).getInputStream())
                .createScoped(List.of(GOOGLE_AUTH_URL));

        // 토큰 만료 ? 갱신 : 토큰
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    // FCM 전송 정보를 기반으로 메시지 구성
    // result값에 따라 합불 처리
    private String makeMessage(Aplict aplict, AplictStatus aplictResult) throws JsonProcessingException {
        // json변환
        ObjectMapper om = new ObjectMapper();

        // 메시지 제목
        String titleMessage = APLICT_TITLE_MESSAGE;

        // 메시지 내용
        String bodyMessage;
        if (aplictResult == AplictStatus.PASS) bodyMessage = aplict.getClub().getClubName() + APLICT_PASS_MESSAGE;
        else if (aplictResult == AplictStatus.FAIL) bodyMessage = aplict.getClub().getClubName() + APLICT_FAIL_MESSAGE;
        else bodyMessage = APLICT_ERROR_MESSAGE;

        // 메시지 구성
        FcmMessageDto fcmMessageDto = FcmMessageDto.builder()
                .message(FcmMessageDto.Message.builder()
                        .token(aplict.getProfile().getFcmToken().trim())
                        .notification(FcmMessageDto.Notification.builder()
                                .title(titleMessage)
                                .body(bodyMessage)
                                .image(null)
                                .build()
                        ).build()).validateOnly(false).build();

        return om.writeValueAsString(fcmMessageDto);
    }

    // fcm token 갱신
    public void refreshFcmToken(FcmTokenRequest fcmTokenRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.user();
        log.debug("인증된 사용자: {}", user.getUserAccount());

        // fcm 토큰 갱신
        Optional<Profile> userFcmTokenOptional = profileRepository.findByUserUserId(user.getUserId());
        if (userFcmTokenOptional.isPresent()) {
            Profile userFcmToken = userFcmTokenOptional.get();
            userFcmToken.updateFcmToken(fcmTokenRequest.getFcmToken());
            profileRepository.save(userFcmToken);
            log.debug("사용자 {}의 FCM 토큰 갱신 완료", user.getUserAccount());
        } else {
            log.warn("사용자 {}의 프로필을 찾을 수 없음. FCM 토큰 갱신 실패", user.getUserAccount());
        }
    }
}
