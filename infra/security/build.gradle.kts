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
    implementation(Dependencies.springBootStarterSecurity)
    implementation(Dependencies.springBootStarterOauth2Client)
    implementation(Dependencies.jakartaServletApi)
    implementation(Dependencies.jjwtApi)
    implementation(Dependencies.jjwtImpl)
    implementation(Dependencies.jjwtJackson)
    implementation(Dependencies.jacksonDatatypeJsr310)
    implementation(Dependencies.jacksonModuleKotlin)
    implementation(Dependencies.springdocOpenApi)
    implementation(Dependencies.keycloakSpringBootAdapter)
    implementation(Dependencies.keycloakSpringSecurityAdapter)

    testImplementation(Dependencies.springBootStarterTest)
}
