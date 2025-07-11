object Versions {
    const val springBoot = "3.2.0"
    const val springDependencyManagement = "1.1.4"
    const val kotlin = "1.9.20"
    const val mongodbDriver = "4.11.1"
    const val jjwt = "0.11.5"
    const val jakartaServlet = "6.0.0"
    const val springdoc = "2.3.0"
    const val thymeleaf = "3.1.2.RELEASE"
    const val keycloak = "23.0.3"
    const val fabric8KubernetesClient = "6.5.1"
    const val testcontainers = "1.19.3"
    const val kotlinxCoroutines = "1.7.3"
}

object Dependencies {
    // Spring
    const val springBootStarter = "org.springframework.boot:spring-boot-starter"
    const val springBootStarterWeb = "org.springframework.boot:spring-boot-starter-web"
    const val springBootStarterSecurity = "org.springframework.boot:spring-boot-starter-security"
    const val springBootStarterData = "org.springframework.boot:spring-boot-starter-data-mongodb"
    const val springBootStarterTest = "org.springframework.boot:spring-boot-starter-test"
    const val springBootStarterOauth2Client = "org.springframework.boot:spring-boot-starter-oauth2-client"
    // WebSocket functionality has been removed
    // const val springBootStarterWebsocket = "org.springframework.boot:spring-boot-starter-websocket"

    // MongoDB
    const val mongodbDriver = "org.mongodb:mongodb-driver-sync:${Versions.mongodbDriver}"

    // JWT
    const val jjwtApi = "io.jsonwebtoken:jjwt-api:${Versions.jjwt}"
    const val jjwtImpl = "io.jsonwebtoken:jjwt-impl:${Versions.jjwt}"
    const val jjwtJackson = "io.jsonwebtoken:jjwt-jackson:${Versions.jjwt}"

    // Kotlin
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect"
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib"

    // Kotlin Coroutines
    const val kotlinxCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinxCoroutines}"
    const val kotlinxCoroutinesReactor = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${Versions.kotlinxCoroutines}"

    // Jakarta
    const val jakartaServletApi = "jakarta.servlet:jakarta.servlet-api:${Versions.jakartaServlet}"

    // Springdoc OpenAPI (Swagger)
    const val springdocOpenApi = "org.springdoc:springdoc-openapi-starter-webmvc-ui:${Versions.springdoc}"

    // Thymeleaf
    const val springBootStarterThymeleaf = "org.springframework.boot:spring-boot-starter-thymeleaf"

    // Keycloak
    const val keycloakSpringBootAdapter = "org.keycloak:keycloak-spring-boot-starter:${Versions.keycloak}"
    const val keycloakSpringSecurityAdapter = "org.keycloak:keycloak-spring-security-adapter:${Versions.keycloak}"

    // Kubernetes
    const val fabric8KubernetesClient = "io.fabric8:kubernetes-client:${Versions.fabric8KubernetesClient}"

    // TestContainers
    const val testcontainersJunit = "org.testcontainers:junit-jupiter:${Versions.testcontainers}"
    const val testcontainersCore = "org.testcontainers:testcontainers:${Versions.testcontainers}"
    const val testcontainersMongodb = "org.testcontainers:mongodb:${Versions.testcontainers}"
    const val testcontainersPostgresql = "org.testcontainers:postgresql:${Versions.testcontainers}"
}
