package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.Secret
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import net.kigawa.keruta.core.domain.model.KubernetesConfig
import net.kigawa.keruta.core.usecase.repository.KubernetesConfigRepository
import net.kigawa.keruta.infra.security.jwt.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Base64

/**
 * Provider for Kubernetes client.
 * Responsible for creating and managing the Kubernetes client based on configuration.
 */
@Component
class KubernetesClientProvider(
    private val kubernetesConfigRepository: KubernetesConfigRepository,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    private val logger = LoggerFactory.getLogger(KubernetesClientProvider::class.java)

    /**
     * Gets the Kubernetes client based on the current configuration.
     * Returns null if Kubernetes integration is disabled or if there's an error creating the client.
     */
    fun getClient() = try {
        val config = kubernetesConfigRepository.getConfig()
        if (config.enabled) {
            if (config.inCluster) {
                logger.info("Using in-cluster Kubernetes configuration")
                KubernetesClientBuilder().build()
            } else if (config.configPath.isNotEmpty()) {
                logger.info("Using Kubernetes configuration from file: ${config.configPath}")
                System.setProperty("kubeconfig", config.configPath)
                KubernetesClientBuilder().build()
            } else {
                logger.info("Using default Kubernetes configuration")
                KubernetesClientBuilder().build()
            }
        } else {
            logger.warn("Kubernetes integration is disabled. Enable it by setting keruta.kubernetes.enabled=true")
            null
        }
    } catch (e: Exception) {
        logger.error("Failed to create Kubernetes client", e)
        null
    }

    /**
     * Gets the current Kubernetes configuration.
     */
    fun getConfig(): KubernetesConfig {
        return kubernetesConfigRepository.getConfig()
    }

    /**
     * Updates the Kubernetes configuration.
     */
    fun updateConfig(config: KubernetesConfig): KubernetesConfig {
        logger.info("Updating Kubernetes configuration: $config")
        return kubernetesConfigRepository.updateConfig(config)
    }

    /**
     * Checks if a secret exists in the specified namespace.
     *
     * @param secretName The name of the secret to check
     * @param namespace The namespace to check in
     * @return true if the secret exists, false otherwise
     */
    fun secretExists(secretName: String, namespace: String): Boolean {
        try {
            val client = getClient() ?: return false
            val secret = client.secrets().inNamespace(namespace).withName(secretName).get()
            return secret != null
        } catch (e: Exception) {
            logger.warn("Failed to check if secret $secretName exists in namespace $namespace", e)
            return false
        }
    }

    /**
     * Creates a secret with the specified name in the specified namespace.
     *
     * @param secretName The name of the secret to create
     * @param namespace The namespace to create the secret in
     * @param data The data to store in the secret
     * @return The created secret, or null if creation failed
     */
    fun createSecret(secretName: String, namespace: String, data: Map<String, String>): Secret? {
        try {
            val client = getClient() ?: return null

            // Create a new Secret object
            val secret = Secret()

            // Set metadata
            val metadata = io.fabric8.kubernetes.api.model.ObjectMeta()
            metadata.name = secretName
            metadata.namespace = namespace
            secret.metadata = metadata

            // Encode the data values in Base64
            val encodedData = mutableMapOf<String, String>()
            data.forEach { (key, value) ->
                encodedData[key] = Base64.getEncoder().encodeToString(value.toByteArray())
            }

            // Set the data
            secret.data = encodedData

            // Create the secret in Kubernetes
            val createdSecret = client.secrets().inNamespace(namespace).create(secret)
            logger.info("Created secret $secretName in namespace $namespace")
            return createdSecret
        } catch (e: Exception) {
            logger.error("Failed to create secret $secretName in namespace $namespace", e)
            return null
        }
    }

    /**
     * Gets or creates the keruta-api-token secret in the specified namespace.
     *
     * @param namespace The namespace to get or create the secret in
     * @return The token value, or null if creation failed
     */
    fun getOrCreateApiTokenSecret(namespace: String): String? {
        val secretName = "keruta-api-token"

        // Check if the secret already exists
        if (secretExists(secretName, namespace)) {
            logger.info("Secret $secretName already exists in namespace $namespace")
            return "existing-token" // We don't actually retrieve the token value for security reasons
        }

        // Generate a JWT token for API access
        val token = jwtTokenProvider.createApiToken("keruta-api")

        // Create the secret
        val data = mapOf("token" to token)
        val createdSecret = createSecret(secretName, namespace, data)

        return if (createdSecret != null) {
            logger.info("Created keruta-api-token secret in namespace $namespace")
            token
        } else {
            logger.error("Failed to create keruta-api-token secret in namespace $namespace")
            null
        }
    }

    /**
     * Updates the keruta-api-token secret in the specified namespace with a new token.
     * If the secret doesn't exist, it will be created.
     *
     * @param namespace The namespace to update or create the secret in
     * @return The new token value, or null if update/creation failed
     */
    fun updateApiTokenSecret(namespace: String): String? {
        val secretName = "keruta-api-token"

        try {
            val client = getClient() ?: return null

            // Generate a new JWT token for API access
            val token = jwtTokenProvider.createApiToken("keruta-api")

            // Encode the token in Base64
            val encodedToken = Base64.getEncoder().encodeToString(token.toByteArray())

            // Check if the secret exists
            if (secretExists(secretName, namespace)) {
                // Get the existing secret
                val existingSecret = client.secrets().inNamespace(namespace).withName(secretName).get()

                // Update the token
                if (existingSecret.data == null) {
                    existingSecret.data = mutableMapOf()
                }
                existingSecret.data["token"] = encodedToken

                // Update the secret in Kubernetes
                client.secrets().inNamespace(namespace).withName(secretName).replace(existingSecret)
                logger.info("Updated keruta-api-token secret in namespace $namespace")
                return token
            } else {
                // Create a new secret
                val data = mapOf("token" to token)
                val createdSecret = createSecret(secretName, namespace, data)

                return if (createdSecret != null) {
                    logger.info("Created keruta-api-token secret in namespace $namespace")
                    token
                } else {
                    logger.error("Failed to create keruta-api-token secret in namespace $namespace")
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to update keruta-api-token secret in namespace $namespace", e)
            return null
        }
    }

    // WebSocket-related methods have been removed
}
