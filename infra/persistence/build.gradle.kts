plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:usecase"))

    implementation(Dependencies.kotlinStdlib)
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.springBootStarter)
    implementation(Dependencies.springBootStarterData)
    implementation(Dependencies.mongodbDriver)

    testImplementation(Dependencies.springBootStarterTest)
}
