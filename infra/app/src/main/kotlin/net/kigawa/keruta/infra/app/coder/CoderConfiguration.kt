package net.kigawa.keruta.infra.app.coder

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
@EnableConfigurationProperties(CoderProperties::class)
class CoderConfiguration {

    @Bean
    fun coderRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        coderProperties: CoderProperties,
    ): RestTemplate {
        return restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(coderProperties.connectionTimeout))
            .setReadTimeout(Duration.ofMillis(coderProperties.readTimeout))
            .build()
    }
}
