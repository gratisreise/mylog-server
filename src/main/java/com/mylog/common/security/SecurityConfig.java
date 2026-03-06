package com.mylog.common.security;

<<<<<<<< HEAD:src/main/java/com/mylog/common/security/SecurityConfig.java
========
import com.mylog.auth.service.TokenBlackListService;
import com.mylog.common.filter.ExceptionHandlerFilter;
import com.mylog.common.filter.JwtAuthenticationFilter;
import com.mylog.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

<<<<<<<< HEAD:src/main/java/com/mylog/common/security/SecurityConfig.java
    private final JwtProvider token;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
========
    private final JwtUtil token;
    private final TokenBlackListService tokenBlackListService;
>>>>>>>> origin/main:api/src/main/java/com/mylog/config/SecurityConfig.java

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(
                session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            //에러 공통규격
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))

            // 요청 정책
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(WHITELIST).permitAll() // 해당 url 허용
                .anyRequest().authenticated() // 나머지 접근 방지
            )
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

            //jwt 커스텀 필터
            .addFilterBefore(new ExceptionHandlerFilter(),
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(
                new JwtAuthenticationFilter(token),
                UsernamePasswordAuthenticationFilter.class)

            //빌드
            .build();
    }


    private static final String[] WHITELIST = {
        "/api/auth/**",
        "/api/members/sign-up",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/h2-console/**",
        "/actuator/**",
        "/api/tests/**",
        "/api/articles/all/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
<<<<<<<< HEAD:src/main/java/com/mylog/common/security/SecurityConfig.java
========

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        return http
            //Stateless한 상태 기본유지
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            // 요청 정책
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(WHITELISTED_URLS).permitAll() // 해당 url 허용
//                .anyRequest().authenticated() // 나머지 접근 방지
//            )
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

            // 세션 설정
            .sessionManagement(
                session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            //jwt 커스텀필터
            .addFilterBefore(new ExceptionHandlerFilter(),
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(
                new JwtAuthenticationFilter(token, userDetailsService, tokenBlackListService),
                UsernamePasswordAuthenticationFilter.class)

            .build();
    }
>>>>>>>> origin/main:api/src/main/java/com/mylog/config/SecurityConfig.java
}
