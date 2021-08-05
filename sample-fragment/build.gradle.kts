plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
}

dependencies {
    commonMainImplementation("city.smartb.f2:f2-dsl-cqrs:${Versions.f2}")
    commonMainImplementation("city.smartb.f2:f2-dsl-function:${Versions.f2}")
    jvmImplementation(project(":sample"))
}
