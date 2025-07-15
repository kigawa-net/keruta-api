plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:usecase"))
    implementation(project(":infra:persistence"))
    implementation(project(":infra:security"))
    implementation(project(":infra:app"))
    implementation(project(":infra:core"))

    implementation(Dependencies.kotlinStdlib)
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.springBootStarter)
    implementation(Dependencies.springBootStarterWeb)
    // WebSocket functionality has been removed
    // implementation(Dependencies.springBootStarterWebsocket)
    implementation(Dependencies.springBootStarterSecurity)
    // WebSocket functionality has been removed
    // implementation("jakarta.websocket:jakarta.websocket-api:2.1.0")

    // Swagger/OpenAPI
    implementation(Dependencies.springdocOpenApi)

    // Thymeleaf
    implementation(Dependencies.springBootStarterThymeleaf)

    // MongoDB
    implementation(Dependencies.springBootStarterData)
    implementation(Dependencies.mongodbDriver)

    testImplementation(Dependencies.springBootStarterTest) {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit}")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(Dependencies.testcontainersJunit)
    testImplementation(Dependencies.testcontainersCore)
    testImplementation(Dependencies.testcontainersMongodb)
    testImplementation(Dependencies.testcontainersPostgresql)
}
