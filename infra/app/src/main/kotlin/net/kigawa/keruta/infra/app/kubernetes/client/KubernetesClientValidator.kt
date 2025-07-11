package net.kigawa.keruta.infra.app.kubernetes.client

import net.kigawa.keruta.infra.app.kubernetes.KubernetesClientProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Validator for Kubernetes client and configuration.
 * Responsible for validating the Kubernetes client and configuration.
 */
@Component
class KubernetesClientValidator(
    private val clientProvider: KubernetesClientProvider,
) {
    private val logger = LoggerFactory.getLogger(KubernetesClientValidator::class.java)

    /**
     * Validates the Kubernetes client and configuration.
     *
     * @return null if validation succeeds, or an error string if it fails
     */
    fun validateClient(): ClientValidateResult {
        val config = clientProvider.getConfig()
        val client = clientProvider.getClient()

        if (!config.enabled || client == null) {
            logger.warn("Kubernetes integration is disabled or client is not available")
            return ClientValidateResult.Error("Kubernetes integration is disabled or client is not available")
        }

        return ClientValidateResult.Success
    }
}
