plugins {
    kotlin("jvm")
}

dependencies {
    implementation(Dependencies.kotlinStdlib)
    implementation(Dependencies.kotlinReflect)

    testImplementation(Dependencies.springBootStarterTest)
}
