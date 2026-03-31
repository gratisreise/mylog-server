package com.mylog.common.security;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final CustomAccessDeniedHandler accessDeniedHandler;
  private final CustomAuthenticationEntryPoint authenticationEntryPoint;
  private final ExceptionHandlerFilter exceptionHandlerFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // 공통응답 형식 맞춰줌
        .exceptionHandling(
            exception ->
                exception
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        // 요청 정책
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(WHITELIST)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/articles/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated() // 나머지 접근 방지
            )
        // jwt 커스텀 필터
        .addFilterBefore(exceptionHandlerFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(
            new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
        // 빌드
        .build();
  }

  // API - 인증/회원
  private static final String[] AUTH_WHITELIST = {
    "/api/auth/register",
    "/api/auth/login",
    "/api/auth/oauth/login",
    "/api/auth/refresh",
    "/api/external/**"
  };

  // 개발 도구
  private static final String[] DEV_WHITELIST = {
    "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**",
  };

  // 모니터링
  private static final String[] MONITOR_WHITELIST = {
    "/actuator/**",
  };

  private static final String[] WHITELIST =
      Stream.of(AUTH_WHITELIST, DEV_WHITELIST, MONITOR_WHITELIST)
          .flatMap(Stream::of)
          .toArray(String[]::new);

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(
        List.of("https://mylog.fyi", "http://localhost:3000", "http://localhost:5300"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
