package net.kigawa.keruta.api.kubernetes.controller

import net.kigawa.keruta.api.kubernetes.dto.KubernetesConfigDto
import net.kigawa.keruta.core.domain.model.KubernetesConfig
import net.kigawa.keruta.core.usecase.kubernetes.KubernetesService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin/kubernetes")
class KubernetesAdminController(
    private val kubernetesService: KubernetesService,
) {

    @GetMapping
    fun kubernetesSettings(model: Model): String {
        model.addAttribute("pageTitle", "Kubernetes Settings")

        // Get the current configuration from the service
        val currentConfig = kubernetesService.getConfig()

        // Convert to DTO
        val configDto = KubernetesConfigDto(
            enabled = currentConfig.enabled,
            configPath = currentConfig.configPath,
            inCluster = currentConfig.inCluster,
            defaultNamespace = currentConfig.defaultNamespace,
            defaultImage = currentConfig.defaultImage,
            defaultPvcStorageSize = currentConfig.defaultPvcStorageSize,
            defaultPvcAccessMode = currentConfig.defaultPvcAccessMode,
            defaultPvcStorageClass = currentConfig.defaultPvcStorageClass,
        )

        model.addAttribute("config", configDto)

        return "admin/kubernetes-settings"
    }

    @PostMapping("/update")
    fun updateKubernetesSettings(@ModelAttribute configDto: KubernetesConfigDto): String {
        // Get the current configuration to preserve the ID
        val currentConfig = kubernetesService.getConfig()

        // Create a new configuration with the updated values
        val updatedConfig = KubernetesConfig(
            id = currentConfig.id,
            enabled = configDto.enabled,
            configPath = configDto.configPath,
            inCluster = configDto.inCluster,
            defaultNamespace = configDto.defaultNamespace,
            defaultImage = configDto.defaultImage,
            defaultPvcStorageSize = configDto.defaultPvcStorageSize,
            defaultPvcAccessMode = configDto.defaultPvcAccessMode,
            defaultPvcStorageClass = configDto.defaultPvcStorageClass,
            createdAt = currentConfig.createdAt,
            updatedAt = currentConfig.updatedAt,
        )

        // Update the configuration through the service
        kubernetesService.updateConfig(updatedConfig)

        return "redirect:/admin/kubernetes"
    }
}
