import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("java-library")
    id("io.spring.dependency-management") version "1.1.4"
}

group = "net.kigawa"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.0")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
    dependsOn(":openApiGenerate")
}

dependencies {
    // Kotlin
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.jetbrains.kotlin:kotlin-reflect")

    // Spring Boot (minimal for API)
    api("org.springframework.boot:spring-boot-starter-web")

    // Jackson for JSON handling
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Validation
    api("jakarta.validation:jakarta.validation-api")
    api("jakarta.annotation:jakarta.annotation-api")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
    }
}