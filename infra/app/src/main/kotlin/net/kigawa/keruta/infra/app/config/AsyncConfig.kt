package net.kigawa.keruta.infra.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * Configuration class for asynchronous task execution.
 * Defines a custom thread pool for handling asynchronous tasks with increased thread counts.
 */
@Configuration(value = "infraAsyncConfig")
@EnableAsync
class AsyncConfig {

    /**
     * Creates a custom thread pool task executor with increased thread counts.
     *
     * @return A configured ThreadPoolTaskExecutor
     */
    @Bean(name = ["infraTaskExecutor"])
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()

        // Set core pool size (number of threads to keep in the pool, even if they are idle)
        executor.corePoolSize = 10

        // Set max pool size (maximum number of threads to allow in the pool)
        executor.maxPoolSize = 50

        // Set queue capacity (size of the queue used for holding tasks before they are executed)
        executor.queueCapacity = 100

        // Set thread name prefix for better identification in logs and monitoring
        executor.setThreadNamePrefix("AsyncTask-")

        // Initialize the executor
        executor.initialize()

        return executor
    }
}
