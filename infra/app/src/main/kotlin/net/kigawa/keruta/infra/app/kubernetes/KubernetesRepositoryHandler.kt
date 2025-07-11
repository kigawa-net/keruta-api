package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.Volume
import io.fabric8.kubernetes.api.model.VolumeMount
import net.kigawa.keruta.core.domain.model.Repository
import net.kigawa.keruta.core.domain.model.Task
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for Git repository setup in Kubernetes jobs.
 * Coordinates the setup of volumes, init containers, and volume mounts for Git repositories.
 */
@Component
class KubernetesRepositoryHandler(
    private val repositoryVolumeHandler: KubernetesRepositoryVolumeHandler,
    private val gitContainerHandler: KubernetesGitContainerHandler,
    private val gitCredentialsHandler: KubernetesGitCredentialsHandler,
) {
    private val logger = LoggerFactory.getLogger(KubernetesRepositoryHandler::class.java)

    data class SetupRepositoryResult(
        val setupped: Boolean,
        val gitCloneContainer: Container?,
        val volumeMount: List<VolumeMount>?,
        val volumes: List<Volume>,
    )

    /**
     * Sets up a Git repository for a Kubernetes job.
     *
     * @param task The task associated with the job
     * @param repository The Git repository to set up
     * @param namespace The Kubernetes namespace
     * @return True if the repository was set up successfully, false otherwise
     */
    fun setupRepository(
        task: Task,
        repository: Repository,
        namespace: String,
        pvcName: String,
    ): SetupRepositoryResult {
        logger.info("Adding init container for git clone: ${repository.url}")

        val repoVolumeName = "repo-volume"
        val repoMountPath = "/work"

        // Create and add repository volume
        val repoVolume = repositoryVolumeHandler.createRepositoryVolume(
            task, repository, namespace, repoVolumeName,
            pvcName,
        )
            ?: return SetupRepositoryResult(
                setupped = false,
                gitCloneContainer = null,
                volumeMount = null,
                volumes = emptyList(),
            )

        // Create git clone container
        val gitCloneContainer = gitContainerHandler.createGitCloneContainer(repository, repoMountPath)

        // Setup git environment variables and handle credentials
        val result = gitCredentialsHandler.setupGitCredentials(
            repository,
            namespace,
            repoVolumeName,
            repoMountPath,
        )?.also {
            gitCloneContainer.env = it.gitEnvVars
            gitCloneContainer.command = it.command
            gitCloneContainer.args = it.args
            gitCloneContainer.volumeMounts = it.volumeMounts
        }

        // Add volume mount to main container
        val addResult = gitContainerHandler.addVolumeToMainContainer(repoVolumeName, repoMountPath)

        return SetupRepositoryResult(
            setupped = true,
            gitCloneContainer = gitCloneContainer,
            volumeMount = addResult,
            volumes = listOfNotNull(repoVolume, result?.credentialsVolume),
        )
    }
}
