
package com.mylog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*") // 모든 도메인 허용
            .allowedMethods("*") // 모든 HTTP 메소드 허용
            .allowedHeaders("*") // 모든 요청 헤더 허용
            .allowCredentials(false) // stateless
            .maxAge(3600);
    }
}
