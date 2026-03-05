package com.centroweg.iot.time_trial_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Primary
    @Bean(name = "eventExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);        // Threads fixas
        executor.setMaxPoolSize(8);         // Máximo de threads
        executor.setQueueCapacity(100);     // Fila de eventos
        executor.setThreadNamePrefix("event-");
        executor.initialize();
        return executor;
    }
}
