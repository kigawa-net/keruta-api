package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.PodSpec
import io.fabric8.kubernetes.api.model.PodTemplateSpec
import io.fabric8.kubernetes.api.model.Volume
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for Kubernetes pod specs.
 * Responsible for creating pod specs for Kubernetes jobs.
 */
@Component
class KubernetesPodSpecHandler {
    private val logger = LoggerFactory.getLogger(KubernetesPodSpecHandler::class.java)

    /**
     * Creates a pod spec.
     *
     * @param containers The list of containers
     * @param volumes The list of volumes
     * @param initContainers The list of init containers
     * @return The created pod spec
     */
    fun createPodSpec(
        containers: List<Container>,
        volumes: List<Volume>,
        initContainers: List<Container>,
    ): PodSpec {
        logger.info("Creating pod spec")

        // Create pod spec
        val podSpec = PodSpec()
        podSpec.containers = containers
        podSpec.volumes = volumes
        podSpec.restartPolicy = "Never" // Do not restart containers on failure

        // Add init containers if any
        if (initContainers.isNotEmpty()) {
            podSpec.initContainers = initContainers
        }

        return podSpec
    }

    /**
     * Creates a pod template spec.
     *
     * @param metadata The pod template metadata
     * @param podSpec The pod spec
     * @return The created pod template spec
     */
    fun createPodTemplateSpec(
        metadata: ObjectMeta,
        podSpec: PodSpec,
    ): PodTemplateSpec {
        logger.info("Creating pod template spec")

        // Create pod template spec
        val podTemplateSpec = PodTemplateSpec()
        podTemplateSpec.metadata = metadata
        podTemplateSpec.spec = podSpec

        return podTemplateSpec
    }
}
