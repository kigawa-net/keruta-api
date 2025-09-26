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
            .cors { it.disable() } // CORS is handled by WebMvcConfigurer
            .authorizeHttpRequests { auth ->
                auth
                    // Allow all requests
                    .anyRequest().permitAll()
            }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
