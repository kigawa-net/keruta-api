package net.kigawa.keruta.infra.app.kubernetes

import org.springframework.stereotype.Component

/**
 * Handler for Kubernetes namespace and job name determination.
 * Responsible for determining the actual namespace and job name to use.
 */
@Component
class KubernetesNamespaceHandler(
    private val clientProvider: KubernetesClientProvider,
) {
    /**
     * Determines the actual namespace and job name to use.
     *
     * @param namespace The requested namespace
     * @param jobName The requested job name
     * @param taskId The task ID
     * @return A pair of the actual namespace and job name
     */
    fun determineNamespaceAndJobName(namespace: String, jobName: String?, taskId: String): Pair<String, String> {
        val config = clientProvider.getConfig()
        val actualNamespace = namespace.ifEmpty { config.defaultNamespace }
        val actualJobName = jobName ?: "keruta-job-$taskId"

        return Pair(actualNamespace, actualJobName)
    }
}
