package net.kigawa.keruta.infra.app.exclusion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kigawa.keruta.core.usecase.exclusion.TaskExecutionExclusionService
import net.kigawa.keruta.core.usecase.exclusion.TaskExecutionLock
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.locks.InterProcessMutex
import org.apache.zookeeper.CreateMode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Service
open class ZooKeeperTaskExecutionExclusionService(
    private val curatorFramework: CuratorFramework,
) : TaskExecutionExclusionService {

    companion object {
        private const val LOCK_PATH_PREFIX = "/task-execution/locks"
        private const val LOCK_TIMEOUT_SECONDS = 30L
    }

    private val logger = LoggerFactory.getLogger(ZooKeeperTaskExecutionExclusionService::class.java)
    private val activeLocks = ConcurrentHashMap<String, InterProcessMutex>()
    private val nodeId = getNodeId()

    override suspend fun acquireLock(taskId: String): TaskExecutionLock? {
        return withContext(Dispatchers.IO) {
            try {
                val lockPath = "$LOCK_PATH_PREFIX/$taskId"
                val mutex = InterProcessMutex(curatorFramework, lockPath)

                logger.debug("Attempting to acquire lock for task: {} on node: {}", taskId, nodeId)

                val acquired = mutex.acquire(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                if (acquired) {
                    activeLocks[taskId] = mutex

                    // Store lock metadata in ZooKeeper
                    val metadataPath = "$lockPath/metadata"
                    val lockData = createLockMetadata(taskId, nodeId)

                    try {
                        if (curatorFramework.checkExists().forPath(metadataPath) != null) {
                            curatorFramework.delete().forPath(metadataPath)
                        }
                        curatorFramework.create()
                            .withMode(CreateMode.EPHEMERAL)
                            .forPath(metadataPath, lockData.toByteArray())
                    } catch (e: Exception) {
                        logger.warn("Failed to create lock metadata, but lock was acquired: {}", e.message)
                    }

                    val lock = TaskExecutionLock(
                        taskId = taskId,
                        nodeId = nodeId,
                        acquiredAt = LocalDateTime.now(),
                        lockPath = lockPath,
                    )

                    logger.info("Successfully acquired lock for task: {} on node: {}", taskId, nodeId)
                    return@withContext lock
                } else {
                    logger.warn("Failed to acquire lock for task: {} within {} seconds", taskId, LOCK_TIMEOUT_SECONDS)
                    return@withContext null
                }
            } catch (e: Exception) {
                logger.error("Error acquiring lock for task: {} - {}", taskId, e.message, e)
                return@withContext null
            }
        }
    }

    override suspend fun releaseLock(lock: TaskExecutionLock) {
        withContext(Dispatchers.IO) {
            try {
                val mutex = activeLocks.remove(lock.taskId)
                if (mutex != null) {
                    mutex.release()

                    // Clean up metadata
                    val metadataPath = "${lock.lockPath}/metadata"
                    try {
                        if (curatorFramework.checkExists().forPath(metadataPath) != null) {
                            curatorFramework.delete().forPath(metadataPath)
                        }
                    } catch (e: Exception) {
                        logger.warn("Failed to clean up lock metadata: {}", e.message)
                    }

                    logger.info("Successfully released lock for task: {} on node: {}", lock.taskId, nodeId)
                } else {
                    logger.warn("No active lock found for task: {} on node: {}", lock.taskId, nodeId)
                }
            } catch (e: Exception) {
                logger.error("Error releasing lock for task: {} - {}", lock.taskId, e.message, e)
            }
        }
    }

    override suspend fun isTaskLocked(taskId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val lockPath = "$LOCK_PATH_PREFIX/$taskId"
                val children = curatorFramework.children.forPath(lockPath)

                // Check if there are any active lock nodes
                val hasActiveLocks = children.any { it.startsWith("_c_") }

                if (hasActiveLocks) {
                    logger.debug("Task {} is currently locked", taskId)
                } else {
                    logger.debug("Task {} is not locked", taskId)
                }

                return@withContext hasActiveLocks
            } catch (e: Exception) {
                logger.debug("Error checking lock status for task: {} - assuming not locked", taskId)
                return@withContext false
            }
        }
    }

    private fun createLockMetadata(taskId: String, nodeId: String): String {
        return """
            {
                "taskId": "$taskId",
                "nodeId": "$nodeId",
                "acquiredAt": "${LocalDateTime.now()}",
                "version": "1.0"
            }
        """.trimIndent()
    }

    private fun getNodeId(): String {
        return try {
            val hostname = InetAddress.getLocalHost().hostName
            val pid = ProcessHandle.current().pid()
            "$hostname-$pid"
        } catch (e: Exception) {
            logger.warn("Failed to get hostname, using fallback node ID: {}", e.message)
            "unknown-${System.currentTimeMillis()}"
        }
    }
}
