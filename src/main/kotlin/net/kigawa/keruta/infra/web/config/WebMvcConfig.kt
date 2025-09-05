package net.kigawa.keruta.infra.web.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC Configuration to ensure proper routing precedence
 * This configuration ensures that REST API endpoints take precedence over static resource handling
 */
@Configuration
@EnableWebMvc
class WebMvcConfig : WebMvcConfigurer {

    /**
     * Configure static resource handlers with proper order to avoid conflicts with API endpoints
     */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Static resources should be served from specific paths only, not catch-all
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
            .setCachePeriod(3600)
        
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/")
            .setCachePeriod(3600)
            
        // Swagger UI resources
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
            .setCachePeriod(3600)
    }
}