package net.kigawa.keruta.infra.security.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration for Jackson ObjectMapper.
 */
@Configuration
class JacksonConfig {

    /**
     * Provides an ObjectMapper bean for JSON serialization/deserialization.
     * Registers JavaTimeModule to handle Java 8 date/time types like LocalDateTime.
     */
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerModule(JavaTimeModule())
            .registerModule(KotlinModule.Builder().build())
    }
}
