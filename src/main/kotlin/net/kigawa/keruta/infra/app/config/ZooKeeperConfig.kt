package net.kigawa.keruta.infra.app.config

import jakarta.annotation.PreDestroy
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
open class ZooKeeperConfig {

    private val logger = LoggerFactory.getLogger(ZooKeeperConfig::class.java)

    @Value("\${keruta.zookeeper.connect-string:localhost:2181}")
    private lateinit var connectString: String

    @Value("\${keruta.zookeeper.session-timeout:30000}")
    private var sessionTimeout: Int = 30000

    @Value("\${keruta.zookeeper.connection-timeout:5000}")
    private var connectionTimeout: Int = 5000

    @Value("\${keruta.zookeeper.retry.base-sleep-time:1000}")
    private var baseSleepTime: Int = 1000

    @Value("\${keruta.zookeeper.retry.max-retries:3}")
    private var maxRetries: Int = 3

    @Value("\${keruta.zookeeper.root-path:/keruta}")
    private lateinit var rootPath: String

    private var curatorFramework: CuratorFramework? = null

    @Bean
    @Primary
    open fun curatorFramework(): CuratorFramework {
        val retryPolicy = ExponentialBackoffRetry(baseSleepTime, maxRetries)

        val framework = CuratorFrameworkFactory.builder()
            .connectString(connectString)
            .sessionTimeoutMs(sessionTimeout)
            .connectionTimeoutMs(connectionTimeout)
            .retryPolicy(retryPolicy)
            .namespace("keruta")
            .build()

        try {
            framework.start()
            framework.blockUntilConnected()

            // Create root path if it doesn't exist
            if (framework.checkExists().forPath("/") == null) {
                framework.create()
                    .creatingParentsIfNeeded()
                    .forPath("/")
            }

            // Create task execution path
            val taskExecutionPath = "/task-execution"
            if (framework.checkExists().forPath(taskExecutionPath) == null) {
                framework.create()
                    .creatingParentsIfNeeded()
                    .forPath(taskExecutionPath)
            }

            logger.info("ZooKeeper client connected successfully to: {}", connectString)
            this.curatorFramework = framework
        } catch (e: Exception) {
            logger.error("Failed to connect to ZooKeeper: {}", e.message, e)
            throw RuntimeException("ZooKeeper connection failed", e)
        }

        return framework
    }

    @PreDestroy
    fun cleanup() {
        curatorFramework?.let { framework ->
            try {
                framework.close()
                logger.info("ZooKeeper client disconnected")
            } catch (e: Exception) {
                logger.error("Error closing ZooKeeper client", e)
            }
        }
    }
}
