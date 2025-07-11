package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.VolumeMount
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for Kubernetes volume mounts.
 * Responsible for setting up volume mounts for containers.
 */
@Component
class KubernetesVolumeMountHandler {
    private val logger = LoggerFactory.getLogger(KubernetesVolumeMountHandler::class.java)

    /**
     * Sets up volume mount for the container.
     *
     * @param volumeName The name of the volume
     * @param mountPath The mount path of the volume
     */
    fun setupVolumeMount(
        volumeName: String,
        mountPath: String,
        existingMounts: List<VolumeMount>,
    ): VolumeMount? {
        // Check if a volume mount with the same path already exists
        val existingMount = existingMounts.find { it.mountPath == mountPath }

        if (existingMount != null) {
            logger.info("Volume mount with path $mountPath already exists, skipping")
            return null
        }

        // Create volume mount for work directory
        val volumeMount = VolumeMount()
        volumeMount.name = volumeName
        volumeMount.mountPath = mountPath

        return volumeMount
    }
}
