package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import net.kigawa.keruta.infra.persistence.entity.TaskEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.Optional

class TaskRepositoryImplTest {

    private lateinit var mongoTaskRepository: MongoTaskRepository
    private lateinit var taskRepositoryImpl: TaskRepositoryImpl

    @BeforeEach
    fun setUp() {
        mongoTaskRepository = mock(MongoTaskRepository::class.java)
        taskRepositoryImpl = TaskRepositoryImpl(mongoTaskRepository)
    }

    @Test
    fun `findAll should return all tasks`() {
        // Given
        val taskEntity1 = createTaskEntity("1", "Task 1")
        val taskEntity2 = createTaskEntity("2", "Task 2")
        `when`(mongoTaskRepository.findAll()).thenReturn(listOf(taskEntity1, taskEntity2))

        // When
        val result = taskRepositoryImpl.findAll()

        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("Task 1", result[0].title)
        assertEquals("2", result[1].id)
        assertEquals("Task 2", result[1].title)
    }

    @Test
    fun `findById should return task when it exists`() {
        // Given
        val taskEntity = createTaskEntity("1", "Task 1")
        `when`(mongoTaskRepository.findById("1")).thenReturn(Optional.of(taskEntity))

        // When
        val result = taskRepositoryImpl.findById("1")

        // Then
        assertEquals("1", result?.id)
        assertEquals("Task 1", result?.title)
    }

    @Test
    fun `findById should return null when task does not exist`() {
        // Given
        `when`(mongoTaskRepository.findById("1")).thenReturn(Optional.empty())

        // When
        val result = taskRepositoryImpl.findById("1")

        // Then
        assertNull(result)
    }

    @Test
    fun `save should save and return the task`() {
        // Given
        val task = createTask("1", "Task 1")
        val taskEntity = TaskEntity.fromDomain(task)
        `when`(mongoTaskRepository.save(any(TaskEntity::class.java))).thenReturn(taskEntity)

        // When
        val result = taskRepositoryImpl.save(task)

        // Then
        assertEquals("1", result.id)
        assertEquals("Task 1", result.title)
        verify(mongoTaskRepository).save(any(TaskEntity::class.java))
    }

    @Test
    fun `deleteById should return true when task exists`() {
        // Given
        `when`(mongoTaskRepository.existsById("1")).thenReturn(true)

        // When
        val result = taskRepositoryImpl.deleteById("1")

        // Then
        assertTrue(result)
        verify(mongoTaskRepository).deleteById("1")
    }

    @Test
    fun `deleteById should return false when task does not exist`() {
        // Given
        `when`(mongoTaskRepository.existsById("1")).thenReturn(false)

        // When
        val result = taskRepositoryImpl.deleteById("1")

        // Then
        assertFalse(result)
        verify(mongoTaskRepository, never()).deleteById(anyString())
    }

    @Test
    fun `findNextInQueue should return the next task in queue`() {
        // Given
        val taskEntity = createTaskEntity("1", "Task 1")
        `when`(mongoTaskRepository.findNextInQueue(TaskStatus.PENDING.name)).thenReturn(taskEntity)

        // When
        val result = taskRepositoryImpl.findNextInQueue()

        // Then
        assertEquals("1", result?.id)
        assertEquals("Task 1", result?.title)
    }

    @Test
    fun `findNextInQueue should return null when queue is empty`() {
        // Given
        `when`(mongoTaskRepository.findNextInQueue(TaskStatus.PENDING.name)).thenReturn(null)

        // When
        val result = taskRepositoryImpl.findNextInQueue()

        // Then
        assertNull(result)
    }

    @Test
    fun `findByStatus should return tasks with the specified status`() {
        // Given
        val taskEntity1 = createTaskEntity("1", "Task 1", TaskStatus.IN_PROGRESS)
        val taskEntity2 = createTaskEntity("2", "Task 2", TaskStatus.IN_PROGRESS)
        `when`(
            mongoTaskRepository.findByStatus(TaskStatus.IN_PROGRESS.name),
        ).thenReturn(listOf(taskEntity1, taskEntity2))

        // When
        val result = taskRepositoryImpl.findByStatus(TaskStatus.IN_PROGRESS)

        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("Task 1", result[0].title)
        assertEquals(TaskStatus.IN_PROGRESS, result[0].status)
        assertEquals("2", result[1].id)
        assertEquals("Task 2", result[1].title)
        assertEquals(TaskStatus.IN_PROGRESS, result[1].status)
    }

    @Test
    fun `updateStatus should update task status and return updated task`() {
        // Given
        val task = createTask("1", "Task 1")
        val updatedTask = task.copy(status = TaskStatus.IN_PROGRESS, updatedAt = LocalDateTime.now())
        val updatedTaskEntity = TaskEntity.fromDomain(updatedTask)

        `when`(mongoTaskRepository.findById("1")).thenReturn(Optional.of(TaskEntity.fromDomain(task)))
        `when`(mongoTaskRepository.save(any(TaskEntity::class.java))).thenReturn(updatedTaskEntity)

        // When
        val result = taskRepositoryImpl.updateStatus("1", TaskStatus.IN_PROGRESS)

        // Then
        assertEquals("1", result.id)
        assertEquals("Task 1", result.title)
        assertEquals(TaskStatus.IN_PROGRESS, result.status)
    }

    @Test
    fun `updateStatus should throw exception when task does not exist`() {
        // Given
        `when`(mongoTaskRepository.findById("1")).thenReturn(Optional.empty())

        // When/Then
        assertThrows<IllegalArgumentException> {
            taskRepositoryImpl.updateStatus("1", TaskStatus.IN_PROGRESS)
        }
    }

    @Test
    fun `updateLogs should append logs and return updated task`() {
        // Given
        val task = createTask("1", "Task 1", logs = "Initial logs")
        val updatedTask = task.copy(logs = "Initial logs\nNew logs", updatedAt = LocalDateTime.now())
        val updatedTaskEntity = TaskEntity.fromDomain(updatedTask)

        `when`(mongoTaskRepository.findById("1")).thenReturn(Optional.of(TaskEntity.fromDomain(task)))
        `when`(mongoTaskRepository.save(any(TaskEntity::class.java))).thenReturn(updatedTaskEntity)

        // When
        val result = taskRepositoryImpl.updateLogs("1", "New logs")

        // Then
        assertEquals("1", result.id)
        assertEquals("Task 1", result.title)
        assertEquals("Initial logs\nNew logs", result.logs)
    }

    @Test
    fun `updateLogs should set logs when task has no logs`() {
        // Given
        val task = createTask("1", "Task 1", logs = null)
        val updatedTask = task.copy(logs = "New logs", updatedAt = LocalDateTime.now())
        val updatedTaskEntity = TaskEntity.fromDomain(updatedTask)

        `when`(mongoTaskRepository.findById("1")).thenReturn(Optional.of(TaskEntity.fromDomain(task)))
        `when`(mongoTaskRepository.save(any(TaskEntity::class.java))).thenReturn(updatedTaskEntity)

        // When
        val result = taskRepositoryImpl.updateLogs("1", "New logs")

        // Then
        assertEquals("1", result.id)
        assertEquals("Task 1", result.title)
        assertEquals("New logs", result.logs)
    }

    @Test
    fun `updateLogs should throw exception when task does not exist`() {
        // Given
        `when`(mongoTaskRepository.findById("1")).thenReturn(Optional.empty())

        // When/Then
        assertThrows<IllegalArgumentException> {
            taskRepositoryImpl.updateLogs("1", "New logs")
        }
    }

    private fun createTaskEntity(id: String, title: String, status: TaskStatus = TaskStatus.PENDING, logs: String? = null): TaskEntity {
        return TaskEntity(
            id = id,
            title = title,
            description = "Description for $title",
            priority = 1,
            status = status.name,
            logs = logs,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }

    private fun createTask(id: String, title: String, status: TaskStatus = TaskStatus.PENDING, logs: String? = null): Task {
        return Task(
            id = id,
            title = title,
            description = "Description for $title",
            priority = 1,
            status = status,
            logs = logs,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            namespace = "test-namespace",
        )
    }
}
