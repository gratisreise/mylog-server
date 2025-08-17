package com.mylog.config;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@Slf4j
public class AsyncConfig implements AsyncConfigurer, AsyncUncaughtExceptionHandler {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 기본 스레드 수
        executor.setMaxPoolSize(10); // 최대 스레드 수
        executor.setQueueCapacity(25); // 큐 용량
        executor.setThreadNamePrefix("Async");
        executor.initialize();
        return executor;
    }

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("❌ Async 메서드에서 예외 발생!");
        log.error("예외: {}", ex.getMessage());
        log.error("메서드명: {}", method.getName());
    }
}
