package net.kigawa.keruta.infra.app.exclusion

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryOneTime
import org.apache.curator.test.TestingServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ZooKeeperTaskExecutionExclusionServiceTest {

    private lateinit var testingServer: TestingServer
    private lateinit var curatorFramework: CuratorFramework
    private lateinit var exclusionService: ZooKeeperTaskExecutionExclusionService

    @BeforeEach
    fun setUp() {
        testingServer = TestingServer()
        testingServer.start()

        curatorFramework = CuratorFrameworkFactory.builder()
            .connectString(testingServer.connectString)
            .retryPolicy(RetryOneTime(1000))
            .namespace("keruta-test")
            .build()

        curatorFramework.start()
        curatorFramework.blockUntilConnected()

        // Create task execution path
        val taskExecutionPath = "/task-execution"
        if (curatorFramework.checkExists().forPath(taskExecutionPath) == null) {
            curatorFramework.create()
                .creatingParentsIfNeeded()
                .forPath(taskExecutionPath)
        }

        exclusionService = ZooKeeperTaskExecutionExclusionService(curatorFramework)
    }

    @AfterEach
    fun tearDown() {
        curatorFramework.close()
        testingServer.close()
    }

    @Test
    fun `should acquire and release lock successfully`() = runBlocking {
        val taskId = "test-task-1"

        // Acquire lock
        val lock = exclusionService.acquireLock(taskId)
        assertNotNull(lock)
        assertEquals(taskId, lock!!.taskId)

        // Verify task is locked
        assertTrue(exclusionService.isTaskLocked(taskId))

        // Release lock
        exclusionService.releaseLock(lock)

        // Verify task is no longer locked
        assertFalse(exclusionService.isTaskLocked(taskId))
    }

    @Test
    fun `should prevent concurrent lock acquisition for same task`() = runBlocking {
        val taskId = "test-task-2"
        val lockCountDown = CountDownLatch(2)
        val results = mutableListOf<Boolean>()

        // Try to acquire lock from two concurrent coroutines
        val jobs = listOf(
            async {
                val lock = exclusionService.acquireLock(taskId)
                val acquired = lock != null
                results.add(acquired)
                if (acquired) {
                    // Hold the lock for a short time
                    Thread.sleep(100)
                    exclusionService.releaseLock(lock!!)
                }
                lockCountDown.countDown()
            },
            async {
                Thread.sleep(50) // Small delay to ensure first coroutine gets the lock first
                val lock = exclusionService.acquireLock(taskId)
                val acquired = lock != null
                results.add(acquired)
                if (acquired) {
                    exclusionService.releaseLock(lock!!)
                }
                lockCountDown.countDown()
            },
        )

        jobs.awaitAll()
        lockCountDown.await(5, TimeUnit.SECONDS)

        // Exactly one should have acquired the lock
        assertEquals(2, results.size)
        assertEquals(1, results.count { it })
        assertEquals(1, results.count { !it })
    }

    @Test
    fun `should allow different tasks to be locked simultaneously`() = runBlocking {
        val taskId1 = "test-task-3"
        val taskId2 = "test-task-4"

        // Acquire locks for different tasks
        val lock1 = exclusionService.acquireLock(taskId1)
        val lock2 = exclusionService.acquireLock(taskId2)

        assertNotNull(lock1)
        assertNotNull(lock2)

        // Both tasks should be locked
        assertTrue(exclusionService.isTaskLocked(taskId1))
        assertTrue(exclusionService.isTaskLocked(taskId2))

        // Release both locks
        exclusionService.releaseLock(lock1!!)
        exclusionService.releaseLock(lock2!!)

        // Both tasks should be unlocked
        assertFalse(exclusionService.isTaskLocked(taskId1))
        assertFalse(exclusionService.isTaskLocked(taskId2))
    }

    @Test
    fun `should return false for isTaskLocked when task is not locked`() = runBlocking {
        val taskId = "non-existent-task"

        assertFalse(exclusionService.isTaskLocked(taskId))
    }

    @Test
    fun `should handle multiple sequential lock acquisitions for same task`() = runBlocking {
        val taskId = "test-task-5"

        // First acquisition
        val lock1 = exclusionService.acquireLock(taskId)
        assertNotNull(lock1)
        assertTrue(exclusionService.isTaskLocked(taskId))

        exclusionService.releaseLock(lock1!!)
        assertFalse(exclusionService.isTaskLocked(taskId))

        // Second acquisition after release
        val lock2 = exclusionService.acquireLock(taskId)
        assertNotNull(lock2)
        assertTrue(exclusionService.isTaskLocked(taskId))

        exclusionService.releaseLock(lock2!!)
        assertFalse(exclusionService.isTaskLocked(taskId))
    }

    @Test
    fun `lock should have valid task metadata`() = runBlocking {
        val taskId = "test-task-6"

        val lock = exclusionService.acquireLock(taskId)
        assertNotNull(lock)

        with(lock!!) {
            assertEquals(taskId, this.taskId)
            assertNotNull(nodeId)
            assertTrue(nodeId.isNotEmpty())
            assertNotNull(acquiredAt)
            assertTrue(lockPath.contains(taskId))
            assertTrue(isValid())
        }

        exclusionService.releaseLock(lock)
    }
}
