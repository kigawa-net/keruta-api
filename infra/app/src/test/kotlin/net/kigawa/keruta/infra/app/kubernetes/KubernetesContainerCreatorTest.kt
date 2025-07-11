package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.EnvVar
import io.fabric8.kubernetes.api.model.VolumeMount
import io.fabric8.kubernetes.client.KubernetesClient
import net.kigawa.keruta.core.domain.model.KubernetesConfig
import net.kigawa.keruta.core.domain.model.Resources
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.core.domain.model.TaskStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.LocalDateTime

class KubernetesContainerCreatorTest {

    private lateinit var containerCreator: KubernetesContainerCreator
    private lateinit var clientProvider: KubernetesClientProvider
    private lateinit var client: KubernetesClient
    private lateinit var config: KubernetesConfig
    private lateinit var task: Task
    private lateinit var resources: Resources

    @BeforeEach
    fun setUp() {
        // Mock KubernetesClientProvider and its dependencies
        clientProvider = mock(KubernetesClientProvider::class.java)
        client = mock(KubernetesClient::class.java)
        config = KubernetesConfig(
            enabled = true,
            defaultNamespace = "test-namespace",
            apiUrl = "http://keruta-api",
        )

        `when`(clientProvider.getClient()).thenReturn(client)
        `when`(clientProvider.getConfig()).thenReturn(config)

        // Create a mock task
        task = mock(Task::class.java)
        `when`(task.id).thenReturn("test-task-id")
        `when`(task.title).thenReturn("Test Task")
        `when`(task.description).thenReturn("Test Description")
        `when`(task.priority).thenReturn(1)
        `when`(task.status).thenReturn(TaskStatus.PENDING)
        `when`(task.createdAt).thenReturn(LocalDateTime.now())
        `when`(task.updatedAt).thenReturn(LocalDateTime.now())

        // Create a mock resources
        resources = mock(Resources::class.java)
        `when`(resources.cpu).thenReturn("100m")
        `when`(resources.memory).thenReturn("128Mi")

        // Create the container creator with the mocked dependencies
        containerCreator = KubernetesContainerCreator(clientProvider, config)
    }

    @Test
    fun `createMainContainer should create a container with the correct configuration when secret exists`() {
        // Given
        val image = "test-image"
        val volumeMounts = emptyList<VolumeMount>()
        val envVars = emptyList<EnvVar>()
        val namespace = "test-namespace"
        val secretName = "keruta-api-token"
        val token = "updated-token"

        // Mock that the secret exists and can be updated
        `when`(clientProvider.updateApiTokenSecret(namespace)).thenReturn(token)

        // When
        val container = containerCreator.createMainContainer(task, image, resources, volumeMounts, envVars)

        // Then
        assertNotNull(container)
        assertEquals("task-container", container.name)
        assertEquals(image, container.image)
        assertEquals(listOf("/bin/sh", "-c"), container.command)
        assertNotNull(container.args)
        assertNotNull(container.env)
        assertNotNull(container.resources)

        // Verify that updateApiTokenSecret was called
        verify(clientProvider).updateApiTokenSecret(namespace)
    }

    @Test
    fun `createMainContainer should create a container with the correct configuration when secret does not exist but can be created`() {
        // Given
        val image = "test-image"
        val volumeMounts = emptyList<VolumeMount>()
        val envVars = emptyList<EnvVar>()
        val namespace = "test-namespace"
        val secretName = "keruta-api-token"
        val token = "test-token"

        // Mock that the secret can be created or updated
        `when`(clientProvider.updateApiTokenSecret(namespace)).thenReturn(token)

        // When
        val container = containerCreator.createMainContainer(task, image, resources, volumeMounts, envVars)

        // Then
        assertNotNull(container)
        assertEquals("task-container", container.name)
        assertEquals(image, container.image)
        assertEquals(listOf("/bin/sh", "-c"), container.command)
        assertNotNull(container.args)
        assertNotNull(container.env)
        assertNotNull(container.resources)

        // Verify that updateApiTokenSecret was called
        verify(clientProvider).updateApiTokenSecret(namespace)
    }

    @Test
    fun `createMainContainer should throw an exception when secret does not exist and cannot be created`() {
        // Given
        val image = "test-image"
        val volumeMounts = emptyList<VolumeMount>()
        val envVars = emptyList<EnvVar>()
        val namespace = "test-namespace"

        // Mock that the secret cannot be updated or created
        `when`(clientProvider.updateApiTokenSecret(namespace)).thenReturn(null)

        // When/Then
        try {
            containerCreator.createMainContainer(task, image, resources, volumeMounts, envVars)
            fail("Expected an IllegalStateException to be thrown")
        } catch (e: IllegalStateException) {
            assertEquals("設定の初期化に失敗しました: 設定の検証に失敗: KERUTA_API_TOKEN が設定されていません", e.message)
        }

        // Verify that updateApiTokenSecret was called
        verify(clientProvider).updateApiTokenSecret(namespace)
    }
}
