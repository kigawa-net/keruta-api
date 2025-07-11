package net.kigawa.keruta.infra.app.kubernetes.job

import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.PodTemplateSpec
import io.fabric8.kubernetes.api.model.batch.v1.Job
import io.fabric8.kubernetes.api.model.batch.v1.JobSpec
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for Kubernetes job specs.
 * Responsible for creating job specs for Kubernetes jobs.
 */
@Component
class KubernetesJobSpecHandler {
    private val logger = LoggerFactory.getLogger(KubernetesJobSpecHandler::class.java)

    /**
     * Creates a job spec.
     *
     * @param podTemplateSpec The pod template spec
     * @return The created job spec
     */
    fun createJobSpec(podTemplateSpec: PodTemplateSpec): JobSpec {
        logger.info("Creating job spec")

        // Create job spec
        val jobSpec = JobSpec()
        jobSpec.backoffLimit = 4 // Number of retries before considering the job failed
        jobSpec.template = podTemplateSpec

        return jobSpec
    }

    /**
     * Creates a job.
     *
     * @param metadata The job metadata
     * @param jobSpec The job spec
     * @return The created job
     */
    fun createJob(metadata: ObjectMeta, jobSpec: JobSpec): Job {
        logger.info("Creating job")

        // Create job
        val job = Job()
        job.metadata = metadata
        job.spec = jobSpec

        return job
    }
}
