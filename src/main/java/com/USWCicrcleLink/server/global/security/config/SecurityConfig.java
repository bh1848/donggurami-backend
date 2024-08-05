package com.USWCicrcleLink.server.global.security.config;

import com.USWCicrcleLink.server.global.security.filter.JwtFilter;
import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import lombok.RequiredArgsConstructor;
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
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/users/log-in", "/admin/login").permitAll(); // 로그인 경로 추가
                    auth.requestMatchers(HttpMethod.POST, "/admin/clubs", "/admin/notices").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/admin/clubs", "/admin/notices", "/admin/notices/paged").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PATCH, "/admin/notices").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/admin/clubs", "/admin/notices").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/apply/").hasRole("USER");
                    auth.requestMatchers(HttpMethod.GET, "/apply/", "/clubs/", "/clubs/intro/").hasRole("USER");
                    auth.requestMatchers(HttpMethod.POST, "/club-leader/info").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.GET, "/club-leader/members", "/club-leader/members/export").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.PATCH, "/club-leader/info", "/club-leader/intro", "/club-leader/toggle-recruitment").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.DELETE, "/club-leader/members").hasRole("LEADER");

                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // 실제 운영 환경에서는 구체적인 도메인으로 제한
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
