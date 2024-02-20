package com.transit.mapbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    private static final int CORE_POOL_SIZE = 15;  // 코어 개수의 1.5배
    private static final int MAX_POOL_SIZE = 40;   // 코어 개수의 4배

    @Bean(name="asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(100);  // 대기 큐 크기
        executor.setThreadNamePrefix("Async-");  // 스레드 이름 접두사
        executor.initialize();

        return executor;
    }

}
