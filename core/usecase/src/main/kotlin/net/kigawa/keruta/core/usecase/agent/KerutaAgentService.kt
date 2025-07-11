/**
 * Service interface for keruta-agent operations.
 */
package net.kigawa.keruta.core.usecase.agent

interface KerutaAgentService {
    /**
     * Gets the URL of the latest release of keruta-agent from GitHub.
     *
     * @return The URL of the latest release
     */
    fun getLatestReleaseUrl(): String
}
