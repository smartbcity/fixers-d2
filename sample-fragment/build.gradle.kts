plugins {
    id("city.smartb.fixers.gradle.kotlin.mpp")
    id("org.jetbrains.dokka")
}

dependencies {
    jvmImplementation(project(":sample"))
}
