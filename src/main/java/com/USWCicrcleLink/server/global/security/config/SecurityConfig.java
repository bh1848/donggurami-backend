package com.USWCicrcleLink.server.global.security.config;

import com.USWCicrcleLink.server.global.security.filter.JwtFilter;
import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Value("${cors.allowed-origins}") // YML 파일에서 allowed-origins 값을 가져옴
    private String allowedOrigin;

    @Bean
    public JwtFilter jwtAuthFilter() {
        return new JwtFilter(jwtProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/users/login", // 모바일 로그인
                            "/users/temporary",
                            "/users/email/verify-token",
                            "/users/finish-signup",
                            "/users/verify-duplicate/{account}",
                            "/users/validate-passwords-match",
                            "/users/find-account/{email}",
                            "/users/auth/send-code",
                            "/users/auth/verify-token",
                            "/users/reset-password",
                            "/users/email/resend-confirmation",
                            "/auth/refresh-token", // 토큰 재발급
                            "/integration/login", // 동아리 회장, 동연회-개발자 통합 로그인
                            "/integration/logout", // 통합 로그아웃
                            "/mainPhoto/**",
                            "/introPhoto/**",
                            "/my-notices/**",
                            "/clubs/**" // 동아리 조회(모바일)
                    ).permitAll();

                    // photo
                    auth.requestMatchers(HttpMethod.GET, "/mainPhoto/**", "/introPhoto/**", "/noticePhoto/**")
//                            .hasAnyRole("USER", "ADMIN", "LEADER");
                            .permitAll();
                    // CLUB(웹)
                    auth.requestMatchers(HttpMethod.POST, "/admin/clubs").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/admin/clubs", "/admin/clubs/{clubId}").hasAnyRole("ADMIN", "LEADER");
                    auth.requestMatchers(HttpMethod.DELETE, "/admin/clubs").hasRole("ADMIN");

                    // NOTICE(웹)
                    auth.requestMatchers(HttpMethod.POST,"/notices").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/notices/{noticeId}", "/notices/paged").hasAnyRole("ADMIN", "LEADER");
                    auth.requestMatchers(HttpMethod.DELETE,"/notices/{noticeId}").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PATCH, "/notices/{noticeId}").hasRole("ADMIN");

                    // APLICT(모바일)
                    auth.requestMatchers(HttpMethod.POST, "/apply/").hasRole("USER");
                    auth.requestMatchers(HttpMethod.GET, "/apply/").hasRole("USER");

                    // USER
                    auth.requestMatchers(HttpMethod.PATCH, "/profiles/change","/users/userpw").hasRole("USER");
                    auth.requestMatchers(HttpMethod.GET,"/my-notices","/mypages/my-clubs","/mypages/aplict-clubs","/profiles/me","/my-notices/{noticeId}/details").hasRole("USER");
                    auth.requestMatchers(HttpMethod.DELETE, "/exit").hasRole("USER");

                    // LEADER
                    auth.requestMatchers(HttpMethod.POST, "/club-leader/{clubId}/**").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.GET, "/club-leader/{clubId}/**").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.PATCH, "/club-leader/{clubId}/**").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.DELETE, "/club-leader/{clubId}/members").hasRole("LEADER");

                    // 기타 모든 요청
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern(allowedOrigin); // 허용할 도메인 패턴
        configuration.addAllowedMethod("GET"); // GET 메서드 허용
        configuration.addAllowedMethod("POST"); // POST 메서드 허용
        configuration.addAllowedMethod("PATCH"); // PATCH 메서드 허용
        configuration.addAllowedMethod("DELETE"); // DELETE 메서드 허용
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 자격 증명 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 CORS 설정 적용
        return source;
    }
}