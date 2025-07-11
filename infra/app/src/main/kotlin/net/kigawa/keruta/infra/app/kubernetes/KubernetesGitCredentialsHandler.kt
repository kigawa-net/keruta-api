package net.kigawa.keruta.infra.app.kubernetes

import io.fabric8.kubernetes.api.model.EnvVar
import io.fabric8.kubernetes.api.model.Volume
import io.fabric8.kubernetes.api.model.VolumeMount
import net.kigawa.keruta.core.domain.model.Repository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for Git credentials in Kubernetes.
 * Responsible for setting up Git credentials and environment variables for repository operations.
 */
@Component
class KubernetesGitCredentialsHandler(
    private val clientProvider: KubernetesClientProvider,
) {
    private val logger = LoggerFactory.getLogger(KubernetesGitCredentialsHandler::class.java)

    data class SetupGitCredentialsResult(
        val gitEnvVars: List<EnvVar>?,
        val command: List<String>?,
        val args: List<String>?,
        val volumeMounts: List<VolumeMount>,
        val credentialsVolume: Volume?,
    )

    /**
     * Sets up git credentials and environment variables for the container.
     *
     * @param repository The Git repository
     * @param namespace The Kubernetes namespace
     * @param volumeName The name of the repository volume
     * @param mountPath The path where the repository volume is mounted
     */
    fun setupGitCredentials(
        repository: Repository,
        namespace: String,
        volumeName: String,
        mountPath: String,
    ): SetupGitCredentialsResult? {
        val client = clientProvider.getClient() ?: return null

        val gitCloneVolumeMount = VolumeMount()
        gitCloneVolumeMount.name = volumeName
        gitCloneVolumeMount.mountPath = mountPath

        val secretName = "git-credentials-${repository.id}"
        try {
            val secret = client.secrets().inNamespace(namespace).withName(secretName).get()
            if (secret == null) {
                return SetupGitCredentialsResult(
                    gitEnvVars = null,
                    command = null,
                    args = null,
                    volumeMounts = listOf(gitCloneVolumeMount),
                    credentialsVolume = null,
                )
            }
            logger.info("Found git credentials secret: $secretName")

            configureContainerWithCredentials(
                repository,
                secretName,
                gitCloneVolumeMount,
                mountPath,
            ).also {
                return SetupGitCredentialsResult(
                    gitEnvVars = it.gitEnvVars,
                    command = it.command,
                    args = it.args,
                    volumeMounts = it.volumeMounts,
                    credentialsVolume = it.credentialsVolume,
                )
            }
        } catch (e: Exception) {
            logger.warn("Failed to get git credentials secret: $secretName", e)
            return SetupGitCredentialsResult(
                gitEnvVars = null,
                command = null,
                args = null,
                volumeMounts = listOf(gitCloneVolumeMount),
                credentialsVolume = null,
            )
        }
    }

    /**
     * Creates basic git environment variables.
     *
     * @return List of environment variables
     */
    private fun createBasicGitEnvVars(): MutableList<EnvVar> {
        return mutableListOf(
            EnvVar("GIT_TERMINAL_PROMPT", "0", null), // Disable interactive prompts
            EnvVar("GIT_CONFIG_COUNT", "2", null),
            EnvVar("GIT_CONFIG_KEY_0", "http.connectTimeout", null),
            EnvVar("GIT_CONFIG_VALUE_0", "30", null),
            EnvVar("GIT_CONFIG_KEY_1", "http.lowSpeedLimit", null),
            EnvVar("GIT_CONFIG_VALUE_1", "1000", null),
        )
    }

    data class ConfigureContainerWithCredentialsResult(
        val gitEnvVars: List<EnvVar>,
        val command: List<String>,
        val args: List<String>,
        val volumeMounts: MutableList<VolumeMount>,
        val credentialsVolume: Volume,
    )

    /**
     * Configures a container with git credentials.
     *
     * @param repository The Git repository
     * @param secretName The name of the secret containing credentials
     * @param repoVolumeMount The volume mount for the repository
     * @param mountPath The path where the repository volume is mounted
     */
    private fun configureContainerWithCredentials(
        repository: Repository,
        secretName: String,
        repoVolumeMount: VolumeMount,
        mountPath: String,
    ): ConfigureContainerWithCredentialsResult {
        val gitEnvVars = createBasicGitEnvVars()

        gitEnvVars.add(createSecretEnvVar("GIT_USERNAME", secretName, "username"))
        gitEnvVars.add(createSecretEnvVar("GIT_PASSWORD", secretName, "password"))

        // Add git config for credential helper using environment variables
        // Update GIT_CONFIG_COUNT to 4 (we're adding 2 more configs)
        gitEnvVars.removeIf { it.name == "GIT_CONFIG_COUNT" }
        gitEnvVars.add(EnvVar("GIT_CONFIG_COUNT", "4", null))

        // Add credential.helper configs
        gitEnvVars.add(EnvVar("GIT_CONFIG_KEY_2", "credential.helper", null))
        gitEnvVars.add(EnvVar("GIT_CONFIG_VALUE_2", "store", null))
        gitEnvVars.add(EnvVar("GIT_CONFIG_KEY_3", "credential.helper", null))
        gitEnvVars.add(EnvVar("GIT_CONFIG_VALUE_3", "cache --timeout=300", null))

        // Add volume for git credentials
        val credentialsVolume = Volume()
        credentialsVolume.name = "git-credentials"
        credentialsVolume.emptyDir = io.fabric8.kubernetes.api.model.EmptyDirVolumeSource()

        // Add volume mount for git credentials
        val credentialsVolumeMount = VolumeMount()
        credentialsVolumeMount.name = "git-credentials"
        credentialsVolumeMount.mountPath = "/git-credentials"

        return ConfigureContainerWithCredentialsResult(
            gitEnvVars = gitEnvVars,
            command = listOf("/bin/sh", "-c"),
            args = listOf(createGitSetupScript(repository.url, mountPath).joinToString("\n")),
            volumeMounts = mutableListOf(repoVolumeMount, credentialsVolumeMount),
            credentialsVolume = credentialsVolume,
        )
    }

    /**
     * Creates an environment variable that references a secret.
     *
     * @param name The name of the environment variable
     * @param secretName The name of the secret
     * @param secretKey The key within the secret
     * @return The created environment variable
     */
    private fun createSecretEnvVar(name: String, secretName: String, secretKey: String): EnvVar {
        val envVar = EnvVar()
        envVar.name = name
        val source = io.fabric8.kubernetes.api.model.EnvVarSource()
        val selector = io.fabric8.kubernetes.api.model.SecretKeySelector()
        selector.name = secretName
        selector.key = secretKey
        source.secretKeyRef = selector
        envVar.valueFrom = source
        return envVar
    }

    /**
     * Creates a script for git setup with credentials.
     *
     * @param repoUrl The URL of the git repository
     * @param mountPath The path where the repository volume is mounted
     * @return List of script lines
     */
    private fun createGitSetupScript(repoUrl: String, mountPath: String): List<String> {
        return listOf(
            "set -e",
            "echo 'Setting up git credentials'",
            "mkdir -p /git-credentials",
            "echo \"https://\$GIT_USERNAME:\$GIT_PASSWORD@github.com\" > /git-credentials/.git-credentials",
            "git config --global credential.helper 'store --file=/git-credentials/.git-credentials'",
            "echo 'Git credentials configured'",
            "git clone $repoUrl $mountPath",
            "echo 'Setting up git exclusions'",
            "echo '/.keruta' >> $mountPath/.git/info/exclude",
            "echo 'Git exclusions configured'",
        )
    }
}
