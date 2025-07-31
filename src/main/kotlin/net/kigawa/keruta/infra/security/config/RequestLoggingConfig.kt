package net.kigawa.keruta.infra.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter

/**
 * Configuration for request logging.
 * This configures the CommonsRequestLoggingFilter to log request details.
 */
@Configuration
class RequestLoggingConfig {

    /**
     * Creates a CommonsRequestLoggingFilter bean.
     * This filter logs request details, including headers, parameters, and payload.
     *
     * @return The configured CommonsRequestLoggingFilter
     */
    @Bean
    fun commonsRequestLoggingFilter(): CommonsRequestLoggingFilter {
        val filter = CommonsRequestLoggingFilter()
        filter.setIncludeQueryString(true)
        filter.setIncludePayload(true)
        filter.setMaxPayloadLength(10000)
        filter.setIncludeHeaders(true)
        filter.setBeforeMessagePrefix("Request [")
        filter.setBeforeMessageSuffix("]")
        filter.setAfterMessagePrefix("Response [")
        filter.setAfterMessageSuffix("]")
        return filter
    }
}
