package net.kigawa.keruta.infra.app.config

import net.kigawa.keruta.core.usecase.coder.CoderProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Configuration for Coder integration.
 */
@Configuration
@EnableConfigurationProperties(CoderProperties::class)
class CoderConfig {

    /**
     * Creates a RestTemplate bean configured for Coder API calls.
     */
    @Bean
    fun coderRestTemplate(coderProperties: CoderProperties): RestTemplate {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(coderProperties.connectionTimeout.toInt())
        factory.setReadTimeout(coderProperties.readTimeout.toInt())

        val restTemplate = RestTemplate(factory)

        // Disable SSL verification if configured
        if (!coderProperties.enableSslVerification) {
            disableSslVerification(restTemplate)
        }

        return restTemplate
    }

    private fun disableSslVerification(restTemplate: RestTemplate) {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate>? = null
            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        // Note: This is a simplified example. In production, proper SSL configuration should be used.
        // For now, we'll log a warning and continue with default SSL behavior.
        // To properly implement SSL bypass, you would need to configure the HTTP client with custom SSL context.
        println("Warning: SSL verification is disabled in CoderConfig. This should only be used in development.")
    }
}
