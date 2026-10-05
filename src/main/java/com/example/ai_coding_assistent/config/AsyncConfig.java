package com.example.ai_coding_assistent.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Enables async processing for repository indexing.
 * Indexing operations (extraction, chunking, embedding) run asynchronously
 * so the upload endpoint can return immediately.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Uses Spring Boot's default SimpleAsyncTaskExecutor.
    // For production, consider configuring a ThreadPoolTaskExecutor
    // with bounded queue and proper thread naming.

    @Bean(name = "taskExecutor") // 👈 This binds directly to @Async("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Configure bounded limits suitable for heavy embedding/chunking tasks
        executor.setCorePoolSize(4); // Base number of concurrent threads
        executor.setMaxPoolSize(8); // Max threads under heavy load
        executor.setQueueCapacity(100); // Queue size for waiting tasks

        // Proper thread naming for easier log debugging
        executor.setThreadNamePrefix("repo-indexer-");

        // Graceful shutdown options
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }
}
