plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

tasks.bootJar {
    enabled = false
}

tasks.bootRun {
    enabled = false
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:usecase"))

    implementation(Dependencies.kotlinStdlib)
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.springBootStarter)

    // Coroutines
    implementation(Dependencies.kotlinxCoroutinesCore)
    implementation(Dependencies.kotlinxCoroutinesReactor)

    testImplementation(Dependencies.springBootStarterTest)
}
