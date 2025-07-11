package net.kigawa.keruta.infra.app.kubernetes.job

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.EnvVar
import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.Volume
import io.fabric8.kubernetes.api.model.VolumeMount
import net.kigawa.keruta.core.domain.model.Resources
import net.kigawa.keruta.core.domain.model.Task
import net.kigawa.keruta.infra.app.kubernetes.KubernetesClientProvider
import net.kigawa.keruta.infra.app.kubernetes.KubernetesContainerHandler
import net.kigawa.keruta.infra.app.kubernetes.KubernetesPodSpecHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Submitter for Kubernetes jobs.
 * Responsible for creating and submitting Kubernetes jobs.
 */
@Component
class KubernetesJobSubmitter(
    private val clientProvider: KubernetesClientProvider,
    private val containerHandler: KubernetesContainerHandler,
    private val podSpecHandler: KubernetesPodSpecHandler,
    private val jobSpecHandler: KubernetesJobSpecHandler,
) {
    private val logger = LoggerFactory.getLogger(KubernetesJobSubmitter::class.java)

    /**
     * Creates and submits the job to Kubernetes.
     *
     * @param task The task
     * @param image The image
     * @param resources The resources
     * @param volumeMounts The volume mounts
     * @param envVars The environment variables
     * @param volumes The volumes
     * @param initContainers The init containers
     * @param metadata The job metadata
     * @param podTemplateMetadata The pod template metadata
     * @param namespace The namespace
     * @return The name of the created job
     */
    fun createAndSubmitJob(
        task: Task,
        image: String,
        resources: Resources?,
        volumeMounts: List<VolumeMount>,
        envVars: List<EnvVar>,
        volumes: MutableList<Volume>,
        initContainers: MutableList<Container>,
        metadata: ObjectMeta,
        podTemplateMetadata: ObjectMeta,
        namespace: String,
    ): String {
        val client = clientProvider.getClient()!!

        // Create pod spec and pod template spec
        val podSpec = podSpecHandler.createPodSpec(
            mutableListOf(
                containerHandler.createMainContainer(task, image, resources, volumeMounts, envVars),
            ),
            volumes,
            initContainers,
        )
        val podTemplateSpec = podSpecHandler.createPodTemplateSpec(podTemplateMetadata, podSpec)

        // Create job spec and job
        val jobSpec = jobSpecHandler.createJobSpec(podTemplateSpec)
        val job = jobSpecHandler.createJob(metadata, jobSpec)

        // Create the job
        val createdJob = client.batch().v1().jobs().inNamespace(namespace).create(job)
        logger.info(
            "Created Kubernetes job: ${createdJob.metadata.name} in namespace: ${createdJob.metadata.namespace}",
        )

        return createdJob.metadata.name
    }
}
