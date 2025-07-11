package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.KubernetesConfig

/**
 * Repository interface for Kubernetes configuration operations.
 */
interface KubernetesConfigRepository {
    /**
     * Gets the current Kubernetes configuration.
     * If no configuration exists, a default one will be created.
     *
     * @return The current Kubernetes configuration
     */
    fun getConfig(): KubernetesConfig

    /**
     * Updates the Kubernetes configuration.
     *
     * @param config The new configuration
     * @return The updated configuration
     */
    fun updateConfig(config: KubernetesConfig): KubernetesConfig
}
