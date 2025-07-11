package net.kigawa.keruta.infra.app.kubernetes

import net.kigawa.keruta.core.domain.model.Repository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class KubernetesGitContainerHandlerTest {

    private lateinit var kubernetesGitContainerHandler: KubernetesGitContainerHandler

    @BeforeEach
    fun setUp() {
        kubernetesGitContainerHandler = KubernetesGitContainerHandler()
    }

    @Test
    fun `createGitCloneContainer should create a container with correct properties`() {
        // Given
        val repository = createRepository("test-repo", "https://github.com/test/repo.git")
        val volumeName = "test-volume"
        val mountPath = "/workspace"

        // When
        val container = kubernetesGitContainerHandler.createGitCloneContainer(repository, mountPath)

        // Then
        assertEquals("git-clone", container.name)
        assertEquals("alpine/git:latest", container.image)
        assertEquals(listOf("/bin/sh", "-c"), container.command)

        // Verify the script contains the expected commands
        val scriptContent = container.args[0]
        assertTrue(scriptContent.contains("set -e"))
        assertTrue(scriptContent.contains("git clone --depth 1 --single-branch ${repository.url} $mountPath"))
        assertTrue(scriptContent.contains("echo '/.keruta' >> $mountPath/.git/info/exclude"))
    }

    @Test
    fun `addVolumeToMainContainer should create volume mount with correct properties`() {
        // Given
        val volumeName = "test-volume"
        val mountPath = "/workspace"

        // When
        val volumeMounts = kubernetesGitContainerHandler.addVolumeToMainContainer(volumeName, mountPath)

        // Then
        assertNotNull(volumeMounts)
        assertEquals(1, volumeMounts.size)
        assertEquals(volumeName, volumeMounts[0].name)
        assertEquals(mountPath, volumeMounts[0].mountPath)
    }

    @Test
    fun `addVolumeToMainContainer should create volume mount with correct properties when called multiple times`() {
        // Given
        val volumeName1 = "test-volume-1"
        val mountPath1 = "/workspace-1"
        val volumeName2 = "test-volume-2"
        val mountPath2 = "/workspace-2"

        // When
        val volumeMounts1 = kubernetesGitContainerHandler.addVolumeToMainContainer(volumeName1, mountPath1)
        val volumeMounts2 = kubernetesGitContainerHandler.addVolumeToMainContainer(volumeName2, mountPath2)

        // Then
        assertNotNull(volumeMounts1)
        assertEquals(1, volumeMounts1.size)
        assertEquals(volumeName1, volumeMounts1[0].name)
        assertEquals(mountPath1, volumeMounts1[0].mountPath)

        assertNotNull(volumeMounts2)
        assertEquals(1, volumeMounts2.size)
        assertEquals(volumeName2, volumeMounts2[0].name)
        assertEquals(mountPath2, volumeMounts2[0].mountPath)
    }

    private fun createRepository(name: String, url: String): Repository {
        return Repository(
            id = "test-id",
            name = name,
            url = url,
            description = "Test repository",
            isValid = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }
}
