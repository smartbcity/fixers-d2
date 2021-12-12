plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
}

dependencies {
    jvmImplementation(project(":sample"))
}
