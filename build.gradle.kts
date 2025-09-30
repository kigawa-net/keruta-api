plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("org.openapi.generator") version "7.1.0"
}

// Configure subprojects to use same plugin versions
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "java-library")

    repositories {
        mavenCentral()
    }
}

group = "net.kigawa"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

// Security: Force resolution strategy for vulnerable dependencies
configurations.all {
    resolutionStrategy {
        // Force latest available version for CVE-2025-48924
        force("org.apache.commons:commons-lang3:3.18.0")

        // Enable dependency verification for security vulnerabilities
        eachDependency {
            if (requested.group == "org.apache.commons" && requested.name == "commons-lang3") {
                if (requested.version!! < "3.18.0") {
                    useVersion("3.18.0")
                    because("CVE-2025-48924 security mitigation - using latest available")
                }
            }
        }
    }
}

dependencies {

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // MongoDB
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:4.11.1")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Swagger/OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0") {
        exclude(group = "org.apache.commons", module = "commons-lang3")
    }

    // Security: CVE-2025-48924 affects commons-lang3 up to 3.18.0
    // Waiting for patched version > 3.18.0 or using runtime protections
    implementation("org.apache.commons:commons-lang3:3.18.0")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.3")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("0.50.0")
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(true)
    filter {
        include("src/**/*.kt")
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// OpenAPI Code Generation Configuration
openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("${projectDir}/src/main/resources/openapi.yaml")
    outputDir.set("${projectDir}/build/generated/openapi")
    apiPackage.set("net.kigawa.keruta.api.generated")
    modelPackage.set("net.kigawa.keruta.model.generated")
    packageName.set("net.kigawa.keruta.generated")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "interfaceOnly" to "true",
            "useTags" to "true",
            "skipDefaultInterface" to "true",
            "documentationProvider" to "none", // Disable SpringDoc to avoid SpringDocConfiguration.kt
            "useSpringBoot3" to "true",
            "serializationLibrary" to "jackson",
        ),
    )
}

// Add generated sources to source set
sourceSets {
    main {
        kotlin {
            srcDir("${layout.buildDirectory.get()}/generated/openapi/src/main/kotlin")
        }
    }
}

// Ensure code generation runs before compilation
tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}

// Ensure ktlint runs after code generation and exclude generated files
tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask> {
    dependsOn("openApiGenerate")
    setSource(files("src"))
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask> {
    dependsOn("openApiGenerate")
    setSource(files("src"))
}
