package com.USWCicrcleLink.server.global.security.config;

import com.USWCicrcleLink.server.global.security.exception.CustomAuthenticationEntryPoint;
import com.USWCicrcleLink.server.global.security.filter.JwtFilter;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.security.jwt.SecurityProperties;
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
    private final SecurityProperties securityProperties;

    @Value("${cors.allowed-origins}") // YML 파일에서 allowed-origins 값을 가져옴
    private String allowedOrigin;

    @Bean
    public JwtFilter jwtAuthFilter() {
        return new JwtFilter(jwtProvider, securityProperties.getPermitAllPaths());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling -> {
                    // 인증 실패에 대한 처리
                    exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint);
                })
                // 공개
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(securityProperties.getPermitAllPaths().toArray(new String[0])).permitAll();

                    // photo
                    auth.requestMatchers(HttpMethod.GET, "/mainPhoto/**", "/introPhoto/**", "/noticePhoto/**")
//                            .hasAnyRole("USER", "ADMIN", "LEADER");
                            .permitAll();
                    // ADMIN - FloorPhoto
                    auth.requestMatchers(HttpMethod.POST, "/admin/floor/photo/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/admin/floor/photo/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/admin/floor/photo/**").hasRole("ADMIN");

                    // ADMIN - Category
                    auth.requestMatchers(HttpMethod.POST, "/admin/clubs/category/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/admin/clubs/category/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/admin/clubs/category/**").hasRole("ADMIN");

                    // ADMIN - Club
                    auth.requestMatchers(HttpMethod.POST, "/admin/clubs").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/admin/clubs", "/admin/clubs/{clubId}").hasAnyRole("ADMIN", "LEADER");
                    auth.requestMatchers(HttpMethod.DELETE, "/admin/clubs").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.GET,"/admin/clubs/leader/check").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.GET,"/admin/clubs/name/check").hasRole("ADMIN");

                    // ADMIN - Notice
                    auth.requestMatchers(HttpMethod.POST, "/notices").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/notices/{noticeId}", "/notices").hasAnyRole("ADMIN", "LEADER");
                    auth.requestMatchers(HttpMethod.DELETE, "/notices/{noticeId}").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PATCH, "/notices/{noticeId}").hasRole("ADMIN");

                    // USER
                    auth.requestMatchers(HttpMethod.PATCH, "/profiles/change","/users/userpw","/club-leader/fcmtoken").hasRole("USER");
                    auth.requestMatchers(HttpMethod.GET,"/my-notices","/mypages/my-clubs","/mypages/aplict-clubs","/profiles/me","/my-notices/{noticeId}/details").hasRole("USER");
                    auth.requestMatchers(HttpMethod.DELETE, "/exit").hasRole("USER");
                    auth.requestMatchers(HttpMethod.POST, "/exit/send-code").hasRole("USER");
                    auth.requestMatchers(HttpMethod.POST, "/apply/**").hasRole("USER");
                    auth.requestMatchers(HttpMethod.GET, "/apply/**").hasRole("USER");

                    // LEADER
                    auth.requestMatchers(HttpMethod.POST, "/club-leader/{clubUUID}/**").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.GET, "/club-leader/{clubUUID}/**").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.PATCH, "/club-leader/{clubUUID}/**").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.DELETE, "/club-leader/{clubUUID}/members").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.POST, "/club-leader/category").hasRole("LEADER");

                    // 기타 모든 요청 인증 필요
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 특정 출처 허용
        configuration.addAllowedOriginPattern(allowedOrigin);

        // 허용할 HTTP 메서드 명시
        configuration.addAllowedMethod(HttpMethod.GET);
        configuration.addAllowedMethod(HttpMethod.POST);
        configuration.addAllowedMethod(HttpMethod.PUT);
        configuration.addAllowedMethod(HttpMethod.PATCH);
        configuration.addAllowedMethod(HttpMethod.DELETE);
        configuration.addAllowedMethod(HttpMethod.OPTIONS); // Preflight 요청에 사용되는 OPTIONS 메서드 허용

        // 허용할 헤더 명시
        configuration.addAllowedHeader("Authorization");
        configuration.addAllowedHeader("Content-Type");
        configuration.addAllowedHeader("X-Requested-With");
        configuration.addAllowedHeader("Accept");
        configuration.addAllowedHeader("Origin");
        configuration.addAllowedHeader("emailToken_uuid");
        configuration.addAllowedHeader("uuid");

        // 자격 증명 허용
        configuration.setAllowCredentials(true);

        // CORS 설정을 모든 경로에 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}