package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder
import io.fabric8.kubernetes.api.model.Volume
import io.fabric8.kubernetes.api.model.VolumeMount
import net.kigawa.keruta.core.domain.model.Task
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

/**
 * Handler for Kubernetes volumes.
 * Responsible for creating and configuring volumes for Kubernetes jobs.
 */
@Component
class KubernetesVolumeHandler(
    private val clientProvider: KubernetesClientProvider,
    private val volumeMountHandler: KubernetesVolumeMountHandler,
    private val taskRepository: net.kigawa.keruta.core.usecase.repository.TaskRepository,
) {
    private val logger = LoggerFactory.getLogger(KubernetesVolumeHandler::class.java)

    /**
     * Creates a work volume and adds it to the volumes list.
     * Also adds a volume mount to the main container.
     * Uses a PersistentVolumeClaim for persistent storage.
     *
     * @param volumes The list of volumes to add to
     * @param mainContainer The main container to add the volume mount to
     * @param namespace The Kubernetes namespace
     * @param task The task for which the volume is being created (optional)
     * @return The name of the created volume
     */
    fun createWorkVolume(
        volumes: MutableList<Volume>,
        mainContainer: Container,
        namespace: String,
        task: Task? = null,
    ): String {
        logger.info("Creating work volume using PVC")

        val volumeName = "work-volume"

        // Check if task has a parent with a PVC
        var parentPvcName: String? = null
        if (task != null) {
            val parentId = task.parentId
            if (parentId != null) {
                val parentTask = taskRepository.findById(parentId)
                if (parentTask != null && parentTask.pvcName != null) {
                    parentPvcName = parentTask.pvcName
                    logger.info("Found parent task with PVC: $parentPvcName")
                }
            }
        }

        // Use parent's PVC if available, otherwise create a new one
        if (parentPvcName != null) {
            logger.info("Using parent's PVC: $parentPvcName")

            // Create volume using parent's PVC
            val workVolume = Volume()
            workVolume.name = volumeName
            val pvcSource = io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource()
            pvcSource.claimName = parentPvcName
            workVolume.persistentVolumeClaim = pvcSource
            volumes.add(workVolume)

            // Add volume mount to main container
            val workVolumeMount = VolumeMount()
            workVolumeMount.name = volumeName
            workVolumeMount.mountPath = "/work"

            // Add volume mount to existing volume mounts or create new list
            if (mainContainer.volumeMounts == null) {
                mainContainer.volumeMounts = mutableListOf(workVolumeMount)
            } else {
                (mainContainer.volumeMounts as MutableList<VolumeMount>).add(workVolumeMount)
            }

            // Update task with parent's PVC name
            if (task != null) {
                val updatedTask = task.copy(pvcName = parentPvcName)
                taskRepository.save(updatedTask)
                logger.info("Updated task ${task.id} with parent's PVC name: $parentPvcName")
            }

            return volumeName
        }

        // Create a new PVC
        val pvcName = "work-pvc-${UUID.randomUUID()}"
        logger.info("Creating new PVC: $pvcName")

        // Get Kubernetes client
        val client = clientProvider.getClient() ?: return volumeName

        // Get Kubernetes config for default values
        val kubernetesConfig = clientProvider.getConfig()

        // Use task's storageClass if provided, otherwise use default from KubernetesConfig
        val storageSize = kubernetesConfig.defaultPvcStorageSize
        val accessMode = kubernetesConfig.defaultPvcAccessMode
        val storageClass = if (task != null && task.storageClass.isNotBlank()) {
            task.storageClass
        } else {
            kubernetesConfig.defaultPvcStorageClass
        }

        logger.info("Using PVC settings - Size: $storageSize, Access Mode: $accessMode, Storage Class: $storageClass")

        // Create PVC
        val pvcBuilder = PersistentVolumeClaimBuilder()
            .withNewMetadata()
            .withName(pvcName)
            .withNamespace(namespace)
            .addToLabels("app", "keruta")
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

        // Create volume using PVC
        val workVolume = Volume()
        workVolume.name = volumeName
        val pvcSource = io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource()
        pvcSource.claimName = pvcName
        workVolume.persistentVolumeClaim = pvcSource
        volumes.add(workVolume)

        // Add volume mount to main container
        val workVolumeMount = VolumeMount()
        workVolumeMount.name = volumeName
        workVolumeMount.mountPath = "/work"

        // Add volume mount to existing volume mounts or create new list
        if (mainContainer.volumeMounts == null) {
            mainContainer.volumeMounts = mutableListOf(workVolumeMount)
        } else {
            (mainContainer.volumeMounts as MutableList<VolumeMount>).add(workVolumeMount)
        }

        // Update task with PVC name
        if (task != null) {
            val updatedTask = task.copy(pvcName = pvcName)
            taskRepository.save(updatedTask)
            logger.info("Updated task ${task.id} with PVC name: $pvcName")
        }

        return volumeName
    }

    /**
     * Mounts an existing PVC to a container.
     * Creates a volume that references the PVC and adds it to the volumes list.
     * Also adds a volume mount to the container.
     *
     * @param volumes The list of volumes to add to
     * @param pvcName The name of the existing PVC to mount
     * @param volumeName The name to use for the volume (optional, defaults to "pvc-volume")
     * @param mountPath The path where the volume should be mounted (optional, defaults to "/pvc")
     * @return The name of the created volume
     */
    fun mountExistingPvc(
        volumes: MutableList<Volume>,
        pvcName: String,
        volumeName: String = "pvc-volume",
        mountPath: String = "/pvc",
        existingMounts: List<VolumeMount>,
    ): Pair<String, VolumeMount?> {
        logger.info("Mounting existing PVC: $pvcName as volume: $volumeName at path: $mountPath")

        // Create volume using existing PVC
        val pvcVolume = Volume()
        pvcVolume.name = volumeName
        val pvcSource = io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource()
        pvcSource.claimName = pvcName
        pvcVolume.persistentVolumeClaim = pvcSource
        volumes.add(pvcVolume)

        // Add volume mount to container
        val volumeMount = volumeMountHandler.setupVolumeMount(volumeName, mountPath, existingMounts)

        return Pair(volumeName, volumeMount)
    }
}
