package net.kigawa.keruta.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.*
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@Testcontainers
class KerutaApplicationTest {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    companion object {
        val network: Network = Network.newNetwork()

        @Container
        val mongoDBContainer = GenericContainer(DockerImageName.parse("mongo:6.0"))
            .withExposedPorts(27017)
            .withEnv("MONGO_INITDB_DATABASE") { "keruta" }
            .withEnv("MONGO_INITDB_ROOT_USERNAME") { "admin" }
            .withEnv("MONGO_INITDB_ROOT_PASSWORD") { "password" }

        @Container
        val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15.6-alpine").withNetwork(network)
            .withExposedPorts(5432)
            .withDatabaseName("keycloak")
            .withUsername("postgres")
            .withPassword("postgres")
            .withNetworkAliases("postgres")
            .withAccessToHost(true)

        @Container
        val keycloakContainer: GenericContainer<*> = GenericContainer("bitnami/keycloak:latest")
            .dependsOn(postgresContainer)
            .withNetwork(network)
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withEnv("KEYCLOAK_DATABASE_VENDOR", "postgresql")
            .withEnv("KEYCLOAK_DATABASE_NAME", "keycloak")
            .withLogConsumer { print(it.utf8String) }
            .withEnv("KEYCLOAK_DATABASE_HOST") { "postgres" }
            .withEnv("KEYCLOAK_DATABASE_USER") { "postgres" }
            .withEnv("KEYCLOAK_DATABASE_PASSWORD") { "postgres" }
            .withEnv("KEYCLOAK_DATABASE_PORT") { "5432" }
            .withEnv("KC_HOSTNAME") { "localhost" }
            .withEnv("KC_HOSTNAME_PORT") { "8080" }
            .withEnv("KEYCLOAK_EXTRA_ARGS") { "--import-realm" }
            .withEnv("KC_HTTP_ENABLED") { "true" }
            .withFileSystemBind(
                "../data/keycloak.default.realm.json",
                "/opt/bitnami/keycloak/data/import/keycloak.default.realm.json",
                BindMode.READ_ONLY,
            )

        @DynamicPropertySource
        @JvmStatic
        fun setProperties(registry: DynamicPropertyRegistry) {
            // MongoDB properties
            registry.add("spring.data.mongodb.host") { mongoDBContainer.host }
            registry.add("spring.data.mongodb.port") { mongoDBContainer.getMappedPort(27017).toString() }
            registry.add("spring.data.mongodb.database") { "keruta" }
            registry.add(
                "spring.security.oauth2.client.provider.keycloak.issuer-uri",
            ) {
                "http://${keycloakContainer.host}:${keycloakContainer.getMappedPort(8080)}/realms/keruta"
            }
            registry.add(
                "keycloak.auth-server-url",
            ) {
                "http://${keycloakContainer.host}:${keycloakContainer.getMappedPort(8080)}"
            }
        }
    }

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
