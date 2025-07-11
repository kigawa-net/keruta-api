plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core:domain"))

    implementation(Dependencies.kotlinStdlib)
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.springBootStarter)

    // Coroutines
    implementation(Dependencies.kotlinxCoroutinesCore)
    implementation(Dependencies.kotlinxCoroutinesReactor)

    testImplementation(Dependencies.springBootStarterTest)
}
