package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder
import io.fabric8.kubernetes.api.model.Volume
import net.kigawa.keruta.core.domain.model.Repository
import net.kigawa.keruta.core.domain.model.Task
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for repository volume operations in Kubernetes.
 * Responsible for creating volumes and persistent volume claims for Git repositories.
 */
@Component
class KubernetesRepositoryVolumeHandler(
    private val clientProvider: KubernetesClientProvider,
) {
    private val logger = LoggerFactory.getLogger(KubernetesRepositoryVolumeHandler::class.java)

    /**
     * Creates a volume for the repository.
     *
     * @param task The task associated with the job
     * @param repository The Git repository
     * @param namespace The Kubernetes namespace
     * @param volumeName The name to use for the volume
     * @return The created volume, or null if creation failed
     */
    fun createRepositoryVolume(
        task: Task,
        repository: Repository,
        namespace: String,
        volumeName: String,
        pvcName: String,
    ): Volume? {
        val repoVolume = Volume()
        repoVolume.name = volumeName

        logger.info("Using PVC for repository: ${repository.name}")

        val client = clientProvider.getClient() ?: return null

        // Check if PVC already exists
        val existingPvc = client.persistentVolumeClaims()
            .inNamespace(namespace)
            .withName(pvcName)
            .get()

        // Create PVC if it doesn't exist
        if (existingPvc == null && task.parentId == null) {
            createPersistentVolumeClaim(client, task, repository, namespace, pvcName)
        } else if (task.parentId != null) {
            logger.info("Using parent task's PVC: $pvcName")
        } else {
            logger.info("Using existing PVC: $pvcName")
        }

        // Set volume to use PVC
        val pvcSource = io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource()
        pvcSource.claimName = pvcName
        repoVolume.persistentVolumeClaim = pvcSource

        return repoVolume
    }

    /**
     * Creates a persistent volume claim for the repository.
     *
     * @param client The Kubernetes client
     * @param task The task associated with the job
     * @param repository The Git repository
     * @param namespace The Kubernetes namespace
     * @param pvcName The name to use for the PVC
     */
    private fun createPersistentVolumeClaim(
        client: io.fabric8.kubernetes.client.KubernetesClient,
        task: Task,
        repository: Repository,
        namespace: String,
        pvcName: String,
    ) {
        logger.info("Creating new PVC: $pvcName")

        // Get Kubernetes config for default values
        val kubernetesConfig = clientProvider.getConfig()

        // Determine PVC settings, using defaults from KubernetesConfig if repository values are empty
        val storageSize = if (repository.pvcStorageSize.isBlank()) kubernetesConfig.defaultPvcStorageSize else repository.pvcStorageSize
        val accessMode = if (repository.pvcAccessMode.isBlank()) kubernetesConfig.defaultPvcAccessMode else repository.pvcAccessMode
        val storageClass = if (repository.pvcStorageClass.isBlank()) kubernetesConfig.defaultPvcStorageClass else repository.pvcStorageClass

        logger.info("Using PVC settings - Size: $storageSize, Access Mode: $accessMode, Storage Class: $storageClass")

        // Create PVC
        val pvcBuilder = PersistentVolumeClaimBuilder()
            .withNewMetadata()
            .withName(pvcName)
            .withNamespace(namespace)
            .addToLabels("app", "keruta")
            .addToLabels("task-id", task.id)
            .endMetadata()
            .withNewSpec()
            .withAccessModes(accessMode)
            .withNewResources()
            .addToRequests("storage", io.fabric8.kubernetes.api.model.Quantity(storageSize))
            .endResources()

        // Set storageClass if provided
        if (storageClass.isNotBlank()) {
            pvcBuilder.withStorageClassName(storageClass)
        }

        val pvc = pvcBuilder.endSpec().build()

        client.persistentVolumeClaims()
            .inNamespace(namespace)
            .create(pvc)
    }
}
