import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.openapitools"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

plugins {
    `java-library`
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework:spring-web:6.1.0")
    implementation("org.springframework:spring-webmvc:6.1.0")

    // Jakarta servlet API
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")

    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.16.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.16.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.0")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}
