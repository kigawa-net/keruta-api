package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.client.KubernetesClient
import net.kigawa.keruta.core.domain.model.KubernetesConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class KubernetesContainerHandlerTest {

    private lateinit var kubernetesContainerHandler: KubernetesContainerHandler
    private lateinit var containerCreator: KubernetesContainerCreator
    private lateinit var scriptExecutionHandler: KubernetesScriptExecutionHandler
    private lateinit var clientProvider: KubernetesClientProvider
    private lateinit var client: KubernetesClient
    private lateinit var config: KubernetesConfig

    @BeforeEach
    fun setUp() {
        // Mock KubernetesClientProvider and its dependencies
        clientProvider = mock(KubernetesClientProvider::class.java)
        client = mock(KubernetesClient::class.java)
        config = KubernetesConfig(
            enabled = true,
            defaultNamespace = "test-namespace",
        )

        `when`(clientProvider.getClient()).thenReturn(client)
        `when`(clientProvider.getConfig()).thenReturn(config)

        // Mock the new dependencies
        containerCreator = mock(KubernetesContainerCreator::class.java)
        scriptExecutionHandler = mock(KubernetesScriptExecutionHandler::class.java)

        // Create the handler with the mocked dependencies
        kubernetesContainerHandler = KubernetesContainerHandler(containerCreator, scriptExecutionHandler)
    }

    @Test
    fun `setupScriptExecution should delegate to scriptExecutionHandler`() {
        // Given
        val container = Container()
        container.name = "test-container"
        container.image = "test-image"
        container.command = listOf("original-command")
        container.args = listOf("original-arg1", "original-arg2")

        val workVolumeName = "test-volume"
        val workMountPath = "/workspace"
        val repositoryId = "repo-123"
        val documentId = "doc-456"
        val agentId = "agent-789"
        val agentInstallCommand = "install-command"
        val agentExecuteCommand = "execute-command"
        val existingMounts = emptyList<io.fabric8.kubernetes.api.model.VolumeMount>()

        // When
        kubernetesContainerHandler.setupScriptExecution(
            workVolumeName,
            workMountPath,
            repositoryId,
            documentId,
            agentId,
            agentInstallCommand,
            agentExecuteCommand,
            existingMounts,
            container,
        )

        // Then
        // Verify that the handler delegates to scriptExecutionHandler
        verify(scriptExecutionHandler).setupScriptExecution(
            workVolumeName,
            workMountPath,
            repositoryId,
            documentId,
            agentId,
            agentInstallCommand,
            agentExecuteCommand,
            existingMounts,
            container,
        )
    }

    @Test
    fun `createMainContainer should delegate to containerCreator`() {
        // Given
        val task = mock(net.kigawa.keruta.core.domain.model.Task::class.java)
        val image = "test-image"
        val resources = mock(net.kigawa.keruta.core.domain.model.Resources::class.java)
        val volumeMounts = emptyList<io.fabric8.kubernetes.api.model.VolumeMount>()
        val envVars = emptyList<io.fabric8.kubernetes.api.model.EnvVar>()

        // When
        kubernetesContainerHandler.createMainContainer(task, image, resources, volumeMounts, envVars)

        // Then
        // Verify that the handler delegates to containerCreator
        verify(containerCreator).createMainContainer(task, image, resources, volumeMounts, envVars)
    }
}
