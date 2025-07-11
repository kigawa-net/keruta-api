package net.kigawa.keruta.infra.security.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration for Jackson ObjectMapper.
 */
@Configuration
class JacksonConfig {

    /**
     * Provides an ObjectMapper bean for JSON serialization/deserialization.
     */
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
    }
}
