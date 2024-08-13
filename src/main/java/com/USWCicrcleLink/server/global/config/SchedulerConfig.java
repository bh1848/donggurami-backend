package com.USWCicrcleLink.server.global.config;
import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.repository.EmailTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

    private final EmailTokenRepository emailTokenRepository;
    private final AplictRepository aplictRepository;


    // 미인증 회원 삭제
    @Scheduled(cron = "0 0 * * * *") // 1시간 마다 실행
    @Transactional
    public void deleteExpiredTokens() {
        long tokenCount = emailTokenRepository.count();
            LocalDateTime time = LocalDateTime.now().minusHours(1); // 만료시간 1시간 경과된 토큰 삭제
            List<EmailToken> tokens = emailTokenRepository.findAllByCertificationTimeBefore(time);
            emailTokenRepository.deleteAll(tokens);
            log.info("만료된 이메일 토큰 삭제: 삭제된 이메일 토큰의 수 =  {}", tokens.size());
    }


    // 매일 00시(자정)에 실행
    // 최초 합 통보 후 4일이 지난 지원서 삭제
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteOldData() {
        // 지원서에는 최초 합 통보 후 4일이 지난 날짜 값만 기입
        // 현재 날짜와 같은 지원서 삭제
        LocalDateTime now = LocalDateTime.now();
        List<Aplict> applicantsToDelete = aplictRepository.findAllByDeleteDateBefore(now);
        aplictRepository.deleteAll(applicantsToDelete);
    }

}
