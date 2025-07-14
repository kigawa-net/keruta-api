package net.kigawa.keruta.infra.security.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Value("\${spring.web.cors.allowed-origins:*}")
    private lateinit var allowedOrigins: String

    @Value("\${spring.web.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private lateinit var allowedMethods: String

    @Value("\${spring.web.cors.allowed-headers:*}")
    private lateinit var allowedHeaders: String

    @Value("\${spring.web.cors.allow-credentials:false}")
    private var allowCredentials: Boolean = false

    @Value("\${spring.web.cors.max-age:3600}")
    private var maxAge: Long = 3600

    @Value("\${spring.web.cors.specific-origins:http://localhost:3000,http://localhost:3001,https://keruta.kigawa.net}")
    private lateinit var specificOriginsString: String

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests { auth ->
                auth
                    // Allow all requests
                    .anyRequest().permitAll()
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // Always include specific origins that need to be allowed
        val specificOrigins = specificOriginsString.split(",").map { it.trim() }

        // If allowedOrigins contains a wildcard, replace it with specific origins
        // Otherwise, add the specific origins to the existing list
        if (allowedOrigins.contains("*")) {
            configuration.allowedOrigins = specificOrigins
        } else {
            val configuredOrigins = allowedOrigins.split(",").map { it.trim() }
            configuration.allowedOrigins = (configuredOrigins + specificOrigins).distinct()
        }

        configuration.allowedMethods = allowedMethods.split(",").map { it.trim() }
        configuration.allowedHeaders = allowedHeaders.split(",").map { it.trim() }
        configuration.allowCredentials = true  // Always allow credentials
        configuration.maxAge = maxAge

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    /**
     * Creates a CorsFilter bean with a high order to ensure it's applied early in the filter chain.
     * This ensures that CORS headers are applied to all responses, including error responses.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun corsFilter(): CorsFilter {
        return CorsFilter(corsConfigurationSource())
    }
}
