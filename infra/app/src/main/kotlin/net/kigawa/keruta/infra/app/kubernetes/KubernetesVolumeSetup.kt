package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.Volume
import io.fabric8.kubernetes.api.model.VolumeMount
import net.kigawa.keruta.core.domain.model.Repository
import net.kigawa.keruta.core.domain.model.Task
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for Kubernetes volume and container setup.
 * Responsible for setting up volumes and containers for Kubernetes jobs.
 */
@Component
class KubernetesVolumeSetup(
    private val repositoryHandler: KubernetesRepositoryHandler,
    private val volumeHandler: KubernetesVolumeHandler,
) {
    private val logger = LoggerFactory.getLogger(KubernetesVolumeSetup::class.java)

    /**
     * Result of volume and container setup.
     */
    data class VolumeSetupResult(
        val volumes: MutableList<Volume>,
        val initContainers: MutableList<Container>,
        val volumeMounts: List<VolumeMount>,
        val workVolumeName: String,
        val workMountPath: String,
    )

    /**
     * Sets up volumes and containers for the job.
     *
     * @param task The task
     * @param repository The repository
     * @param namespace The namespace
     * @param pvcName The PVC name
     * @return A data class containing volumes, init containers, volume mounts, work volume name, and work mount path
     */
    fun setupVolumesAndContainers(
        task: Task,
        repository: Repository?,
        namespace: String,
        pvcName: String,
    ): VolumeSetupResult {
        val volumes = mutableListOf<Volume>()
        val initContainers = mutableListOf<Container>()
        val pvcMountPath = "/pvc"
        var volumeMounts = listOf<VolumeMount>()

        val workVolumeName = if (repository != null) {
            // Use repository volume if available
            repositoryHandler.setupRepository(
                task,
                repository,
                namespace,
                pvcName,
            ).also { result ->
                result.volumeMount?.let { it -> volumeMounts = it }
                result.gitCloneContainer?.let { element -> initContainers.add(element) }
                volumes.addAll(result.volumes)
            }
            "repo-volume" // Use the volume name from repositoryHandler
        } else {
            // Mount existing PVC if specified
            logger.info("Mounting existing PVC: $pvcName at path: $pvcMountPath")
            val mountExistingPvcResult = volumeHandler.mountExistingPvc(
                volumes,
                pvcName,
                "pvc-volume",
                pvcMountPath,
                volumeMounts,
            )
            mountExistingPvcResult.second?.let { volumeMounts += it }
            "pvc-volume" // Use the volume name from volumeHandler
        }

        return VolumeSetupResult(volumes, initContainers, volumeMounts, workVolumeName, pvcMountPath)
    }
}
