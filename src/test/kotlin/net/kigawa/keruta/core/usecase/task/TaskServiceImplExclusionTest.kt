package net.kigawa.keruta.core.usecase.task

import kotlinx.coroutines.runBlocking
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.core.usecase.exclusion.TaskExecutionExclusionService
import net.kigawa.keruta.core.usecase.exclusion.TaskExecutionLock
import net.kigawa.keruta.core.usecase.repository.TaskRepository
import net.kigawa.keruta.core.usecase.submodule.SubmoduleService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TaskServiceImplExclusionTest {

    @Mock
    private lateinit var taskRepository: TaskRepository

    @Mock
    private lateinit var taskLogService: TaskLogService

    @Mock
    private lateinit var submoduleService: SubmoduleService

    @Mock
    private lateinit var exclusionService: TaskExecutionExclusionService

    private lateinit var taskService: TaskServiceImpl

    private val testTask = Task(
        id = "test-task-1",
        sessionId = "session-1",
        name = "Test Task",
        description = "Test Description",
        status = TaskStatus.PENDING,
        script = "echo 'test'",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
    )

    @BeforeEach
    fun setUp() {
        taskService = TaskServiceImpl(
            taskRepository,
            taskLogService,
            submoduleService,
            exclusionService,
        )
    }

    @Test
    fun `should acquire lock when updating task status to IN_PROGRESS`() = runBlocking {
        val taskId = "test-task-1"
        val lock = TaskExecutionLock(
            taskId = taskId,
            nodeId = "test-node",
            acquiredAt = LocalDateTime.now(),
            lockPath = "/task-execution/locks/$taskId",
        )

        whenever(taskRepository.findById(taskId)).thenReturn(testTask)
        whenever(exclusionService.acquireLock(taskId)).thenReturn(lock)
        whenever(taskRepository.save(any<Task>())).thenAnswer { it.arguments[0] as Task }

        val result = taskService.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS)

        assertNotNull(result)
        assertEquals(TaskStatus.IN_PROGRESS, result!!.status)
        verify(exclusionService).acquireLock(taskId)
        verify(taskRepository).save(any<Task>())
    }

    @Test
    fun `should fail task when lock acquisition fails`() = runBlocking {
        val taskId = "test-task-2"

        whenever(taskRepository.findById(taskId)).thenReturn(testTask)
        whenever(exclusionService.acquireLock(taskId)).thenReturn(null)
        whenever(taskRepository.save(any<Task>())).thenAnswer { it.arguments[0] as Task }

        val result = taskService.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS)

        assertNotNull(result)
        assertEquals(TaskStatus.FAILED, result!!.status)
        assertEquals("Failed to acquire execution lock", result.message)
        assertEquals("LOCK_ACQUISITION_FAILED", result.errorCode)
        verify(exclusionService).acquireLock(taskId)
        verify(taskRepository).save(any<Task>())
    }

    @Test
    fun `should not acquire lock for non-IN_PROGRESS status updates`() = runBlocking {
        val taskId = "test-task-3"

        whenever(taskRepository.findById(taskId)).thenReturn(testTask)
        whenever(taskRepository.save(any<Task>())).thenAnswer { it.arguments[0] as Task }

        val result = taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED, "Task completed")

        assertNotNull(result)
        assertEquals(TaskStatus.COMPLETED, result!!.status)
        assertEquals("Task completed", result.message)
        verify(exclusionService, never()).acquireLock(any())
        verify(taskRepository).save(any<Task>())
    }

    @Test
    fun `executeTaskWithLock should acquire lock and execute task`() = runBlocking {
        val taskId = "test-task-4"
        val lock = TaskExecutionLock(
            taskId = taskId,
            nodeId = "test-node",
            acquiredAt = LocalDateTime.now(),
            lockPath = "/task-execution/locks/$taskId",
        )
        var executionCalled = false

        whenever(exclusionService.acquireLock(taskId)).thenReturn(lock)

        val result = taskService.executeTaskWithLock(taskId) {
            executionCalled = true
        }

        assertTrue(result)
        assertTrue(executionCalled)
        verify(exclusionService).acquireLock(taskId)
        verify(exclusionService).releaseLock(lock)
    }

    @Test
    fun `executeTaskWithLock should return false when lock acquisition fails`() = runBlocking {
        val taskId = "test-task-5"
        var executionCalled = false

        whenever(exclusionService.acquireLock(taskId)).thenReturn(null)

        val result = taskService.executeTaskWithLock(taskId) {
            executionCalled = true
        }

        assertFalse(result)
        assertFalse(executionCalled)
        verify(exclusionService).acquireLock(taskId)
        verify(exclusionService, never()).releaseLock(any())
    }

    @Test
    fun `executeTaskWithLock should release lock even when execution throws exception`() = runBlocking {
        val taskId = "test-task-6"
        val lock = TaskExecutionLock(
            taskId = taskId,
            nodeId = "test-node",
            acquiredAt = LocalDateTime.now(),
            lockPath = "/task-execution/locks/$taskId",
        )

        whenever(exclusionService.acquireLock(taskId)).thenReturn(lock)

        assertThrows(RuntimeException::class.java) {
            runBlocking {
                taskService.executeTaskWithLock(taskId) {
                    throw RuntimeException("Test exception")
                }
            }
        }

        verify(exclusionService).acquireLock(taskId)
        verify(exclusionService).releaseLock(lock)
    }

    @Test
    fun `isTaskCurrentlyExecuting should delegate to exclusion service`() = runBlocking {
        val taskId = "test-task-7"

        whenever(exclusionService.isTaskLocked(taskId)).thenReturn(true)

        val result = taskService.isTaskCurrentlyExecuting(taskId)

        assertTrue(result)
        verify(exclusionService).isTaskLocked(taskId)
    }
}
