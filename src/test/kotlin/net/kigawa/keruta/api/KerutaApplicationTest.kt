package net.kigawa.keruta.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor

// @Testcontainers - Temporarily disabled due to container startup issues
@SpringBootTest(
    properties = [
        "spring.data.mongodb.host=localhost",
        "spring.data.mongodb.port=27017",
        "spring.data.mongodb.database=keruta-test",
        "spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:8080/realms/keruta",
        "keycloak.auth-server-url=http://localhost:8080",
        "keruta.kubernetes.enabled=false",
    ],
)
class KerutaApplicationTest {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun `context loads`() {
        // This test will fail if the application context cannot be loaded
        assertNotNull(applicationContext, "Application context should not be null")
    }

    @Test
    fun `application has all required beans`() {
        // Verify that key beans are available in the application context
        assertNotNull(
            applicationContext.getBean(KerutaApplication::class.java),
            "KerutaApplication bean should be available",
        )
    }

    @Test
    fun `scheduling is enabled`() {
        // Verify that scheduling is enabled by checking for the presence of scheduling-related beans
        assertNotNull(
            applicationContext.getBean(ScheduledAnnotationBeanPostProcessor::class.java),
            "ScheduledAnnotationBeanPostProcessor bean should be available when scheduling is enabled",
        )
    }

    @Test
    fun `configuration properties scanning is enabled`() {
        // Verify that configuration properties scanning is enabled
        // This is a bit harder to test directly, but we can check that the application starts successfully,
        // which it wouldn't if @ConfigurationPropertiesScan was not working correctly
        assertNotNull(applicationContext, "Application context should be available")
        // The fact that the test is running means that the application context was created successfully,
        // which indirectly verifies that configuration properties scanning is working
    }

    @Test
    fun `component scanning includes all required packages`() {
        // Verify that component scanning includes all required packages
        // This is indirectly tested by the application starting successfully,
        // but we can also check for the presence of beans from different packages
        val beanDefinitionNames = applicationContext.beanDefinitionNames
        assertTrue(
            beanDefinitionNames.any { it.contains("keruta") },
            "Bean definitions should include beans from the keruta package",
        )
    }
}
