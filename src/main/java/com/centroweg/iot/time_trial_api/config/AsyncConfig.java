package com.centroweg.iot.time_trial_api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Primary
    @Bean(name = "eventExecutor")
    public Executor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("event-");
        executor.setRejectedExecutionHandler((r, e) ->
                log.error("Evento descartado — pool saturado. Aumente QueueCapacity ou MaxPoolSize.")
        );
        executor.initialize();
        return executor;
    }

    @Bean(name = "notifierExecutor")
    public Executor notifierExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("notifier-");
        executor.setRejectedExecutionHandler((r, e) ->
                log.error("Notificação descartada — fila do notifier saturada.")
        );
        executor.initialize();
        return executor;
    }
}
