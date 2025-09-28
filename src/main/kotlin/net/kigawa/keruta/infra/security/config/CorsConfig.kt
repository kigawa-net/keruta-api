package net.kigawa.keruta.infra.security.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig : WebMvcConfigurer {

    @Value("\${cors.allowed-origins:http://localhost:3000,http://localhost:3001,https://keruta.kigawa.net,https://keruta-dev.kigawa.net,https://keruta-dev-api.kigawa.net}")
    private lateinit var allowedOrigins: String

    @Value("\${cors.allow-credentials:true}")
    private var allowCredentials: Boolean = true

    override fun addCorsMappings(registry: CorsRegistry) {
        val origins = allowedOrigins.split(",").map { it.trim() }.toTypedArray()
        // SSE endpoints - specific origins only
        registry.addMapping("/api/v1/sessions/realtime/**")
            .allowedOrigins(*origins)
            .allowedMethods("GET", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false)
            .maxAge(3600)

        // General API endpoints - specific origins only
        registry.addMapping("/api/**")
            .allowedOrigins(*origins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(allowCredentials)
            .maxAge(3600)
    }
}
