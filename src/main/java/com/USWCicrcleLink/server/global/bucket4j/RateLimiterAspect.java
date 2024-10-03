package com.USWCicrcleLink.server.global.bucket4j;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.RateLimitExceededException;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimiterAspect {

    private final APIRateLimiter apiRateLimiter;
    private final UserService userService;
    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimite rateLimit) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String sessionId = request.getSession(true).getId();
        String clientIp= getClientIp(request);

        String clientId = clientIp+":"+sessionId;
        String action = rateLimit.action();


       if(action.equals("WITHDRAWAL_EMAIL") || action.equals("WITHDRAWAL_CODE")){
           User user = userService.getUserByAuth();
           clientId = String.valueOf(user.getUserUUID());
       }

       // key 생성
        String bucketId= clientId+":"+action;

        if (apiRateLimiter.tryConsume(bucketId,RateLimitAction.valueOf(action))) {
            apiRateLimiter.logBucketState(bucketId, RateLimitAction.valueOf(action));
            return joinPoint.proceed();
        } else {
            throw new RateLimitExceededException(ExceptionType.TOO_MANY_ATTEMPT);  // 토큰이 없는 경우 예외 발생
        }
    }

    // 클라이언트 ip 주소 가져오기
    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();  // 여러 IP가 있을 경우 첫 번째 IP 추출
        }
        if (ip == null || ip.isEmpty()) {
            ip = req.getRemoteAddr();  // X-Forwarded-For 헤더가 없을 경우 기본 IP
        }
        return ip;
    }
}