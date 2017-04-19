package org.dizitart.no2.datagate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Asynchronous operation configuration.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@EnableAsync
@Configuration
public class AsyncConfig extends AsyncConfigurerSupport {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("SyncLog-");
        executor.initialize();
        return executor;
    }
}
