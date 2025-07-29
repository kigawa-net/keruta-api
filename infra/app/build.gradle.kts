plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

tasks.bootJar {
    enabled = false
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:usecase"))
    implementation(project(":infra:persistence"))
    implementation(project(":infra:security"))

    implementation(Dependencies.kotlinStdlib)
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.springBootStarter)
    implementation(Dependencies.springBootStarterWeb)

    // Swagger/OpenAPI
    implementation(Dependencies.springdocOpenApi)

    testImplementation(Dependencies.springBootStarterTest)
    testImplementation(Dependencies.mockitoKotlin)
}
