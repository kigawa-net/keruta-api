plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

// Suppress the warning about missing plugin descriptors
// since we're only using buildSrc for dependency management
gradlePlugin {
    plugins {
        register("empty") {
            id = "empty"
            implementationClass = "EmptyPlugin"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
