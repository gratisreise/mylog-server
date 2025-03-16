package com.mylog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)  // CSRF 보호 비활성화
            .cors(AbstractHttpConfigurer::disable)  // CORS 비활성화
            .formLogin(AbstractHttpConfigurer::disable)  // 폼 로그인 비활성화
            .httpBasic(AbstractHttpConfigurer::disable)  // HTTP Basic 인증 비활성화

            // 모든 요청 허용
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
            )
            //h2 콘솔 프레임 표시 허용
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable()))
            // 세션 설정
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }
}