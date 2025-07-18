package net.kigawa.keruta.infra.persistence.entity

import net.kigawa.keruta.core.domain.model.Document
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TaskEntityTest {

    @Test
    fun `fromDomain should correctly map Task to TaskEntity`() {
        // Given
        val now = LocalDateTime.now()
        val task = Task(
            id = "123",
            title = "Test Task",
            description = "Test Description",
            priority = 1,
            status = TaskStatus.IN_PROGRESS,
            // repository property removed from Task model
            documents = listOf(
                Document(
                    title = "Test Document",
                    content = "Test Content",
                ),
            ),
            image = "test-image",
            namespace = "test-namespace",
            jobName = "test-job",
            podName = "test-pod",
            // resources parameter removed from Task model
            additionalEnv = mapOf("KEY" to "VALUE"),
            logs = "Test logs",
            parentId = "456",
            session = "test-session",
            kubernetesManifest = "test-manifest",
            createdAt = now,
            updatedAt = now,
        )

        // When
        val taskEntity = TaskEntity.fromDomain(task)

        // Then
        assertEquals("123", taskEntity.id)
        assertEquals("Test Task", taskEntity.title)
        assertEquals("Test Description", taskEntity.description)
        assertEquals(1, taskEntity.priority)
        assertEquals(TaskStatus.IN_PROGRESS.name, taskEntity.status)
        assertEquals(null, taskEntity.gitRepository) // Updated to null since repository property was removed
        assertEquals("Test Content", taskEntity.document)
        assertEquals("test-image", taskEntity.image)
        assertEquals("test-namespace", taskEntity.namespace)
        assertEquals("test-job", taskEntity.jobName)
        assertEquals("test-pod", taskEntity.podName)
        assertEquals(null, taskEntity.cpuResource) // Updated since resources property was removed
        assertEquals(null, taskEntity.memoryResource) // Updated since resources property was removed
        assertEquals(mapOf("KEY" to "VALUE"), taskEntity.additionalEnv)
        assertEquals("Test logs", taskEntity.logs)
        assertEquals("456", taskEntity.parentId)
        assertEquals("test-manifest", taskEntity.kubernetesManifest)
        assertEquals(now, taskEntity.createdAt)
        assertEquals(now, taskEntity.updatedAt)
    }

    @Test
    fun `toDomain should correctly map TaskEntity to Task`() {
        // Given
        val now = LocalDateTime.now()
        val taskEntity = TaskEntity(
            id = "123",
            title = "Test Task",
            description = "Test Description",
            priority = 1,
            status = TaskStatus.IN_PROGRESS.name,
            gitRepository = null, // Updated to null since repository property was removed
            document = "Test Content",
            image = "test-image",
            namespace = "test-namespace",
            jobName = "test-job",
            podName = "test-pod",
            cpuResource = "100m",
            memoryResource = "128Mi",
            additionalEnv = mapOf("KEY" to "VALUE"),
            logs = "Test logs",
            parentId = "456",
            kubernetesManifest = "test-manifest",
            createdAt = now,
            updatedAt = now,
        )

        // When
        val task = taskEntity.toDomain()

        // Then
        assertEquals("123", task.id)
        assertEquals("Test Task", task.title)
        assertEquals("Test Description", task.description)
        assertEquals(1, task.priority)
        assertEquals(TaskStatus.IN_PROGRESS, task.status)
        // repository property removed from Task model
        assertEquals(1, task.documents.size)
        assertEquals("Test Task", task.documents[0].title)
        assertEquals("Test Content", task.documents[0].content)
        assertEquals("test-image", task.image)
        assertEquals("test-namespace", task.namespace)
        assertEquals("test-job", task.jobName)
        assertEquals("test-pod", task.podName)
        // resources property removed from Task model
        assertEquals(mapOf("KEY" to "VALUE"), task.additionalEnv)
        assertEquals("Test logs", task.logs)
        assertEquals("456", task.parentId)
        assertEquals("test-manifest", task.kubernetesManifest)
        assertEquals(now, task.createdAt)
        assertEquals(now, task.updatedAt)
    }

    @Test
    fun `toDomain should handle null values correctly`() {
        // Given
        val now = LocalDateTime.now()
        val taskEntity = TaskEntity(
            id = "123",
            title = "Test Task",
            description = null,
            priority = 1,
            status = TaskStatus.PENDING.name,
            gitRepository = null,
            document = null,
            image = null,
            namespace = "default",
            jobName = null,
            podName = null,
            cpuResource = null,
            memoryResource = null,
            additionalEnv = emptyMap(),
            logs = null,
            parentId = null,
            session = "test-session",
            kubernetesManifest = null,
            createdAt = now,
            updatedAt = now,
        )

        // When
        val task = taskEntity.toDomain()

        // Then
        assertEquals("123", task.id)
        assertEquals("Test Task", task.title)
        assertEquals(null, task.description)
        assertEquals(1, task.priority)
        assertEquals(TaskStatus.PENDING, task.status)
        // repository property removed from Task model
        assertEquals(emptyList<Document>(), task.documents)
        assertEquals(null, task.image)
        assertEquals("default", task.namespace)
        assertEquals(null, task.jobName)
        assertEquals(null, task.podName)
        // resources property removed from Task model
        assertEquals(emptyMap<String, String>(), task.additionalEnv)
        assertEquals(null, task.logs)
        assertEquals(null, task.parentId)
        assertEquals(null, task.kubernetesManifest)
        assertEquals(now, task.createdAt)
        assertEquals(now, task.updatedAt)
    }

    @Test
    fun `fromDomain and toDomain should be reversible`() {
        // Given
        val now = LocalDateTime.now()
        val originalTask = Task(
            id = "123",
            title = "Test Task",
            description = "Test Description",
            priority = 1,
            status = TaskStatus.IN_PROGRESS,
            // repository property removed from Task model
            documents = listOf(
                Document(
                    title = "Test Document",
                    content = "Test Content",
                ),
            ),
            image = "test-image",
            namespace = "test-namespace",
            jobName = "test-job",
            podName = "test-pod",
            // resources parameter removed from Task model
            additionalEnv = mapOf("KEY" to "VALUE"),
            logs = "Test logs",
            parentId = "456",
            session = "test-session",
            kubernetesManifest = "test-manifest",
            createdAt = now,
            updatedAt = now,
        )

        // When
        val taskEntity = TaskEntity.fromDomain(originalTask)
        val convertedTask = taskEntity.toDomain()

        // Then
        assertEquals(originalTask.id, convertedTask.id)
        assertEquals(originalTask.title, convertedTask.title)
        assertEquals(originalTask.description, convertedTask.description)
        assertEquals(originalTask.priority, convertedTask.priority)
        assertEquals(originalTask.status, convertedTask.status)
        // repository property removed from Task model
        assertEquals(originalTask.image, convertedTask.image)
        assertEquals(originalTask.namespace, convertedTask.namespace)
        assertEquals(originalTask.jobName, convertedTask.jobName)
        assertEquals(originalTask.podName, convertedTask.podName)
        // resources property removed from Task model
        assertEquals(originalTask.additionalEnv, convertedTask.additionalEnv)
        assertEquals(originalTask.logs, convertedTask.logs)
        assertEquals(originalTask.parentId, convertedTask.parentId)
        assertEquals(originalTask.kubernetesManifest, convertedTask.kubernetesManifest)
        assertEquals(originalTask.createdAt, convertedTask.createdAt)
        assertEquals(originalTask.updatedAt, convertedTask.updatedAt)
    }
}
