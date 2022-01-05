plugins {
    id("city.smartb.fixers.gradle.kotlin.jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation("city.smartb.f2:f2-dsl-cqrs:${Versions.f2}")
    implementation("city.smartb.f2:f2-dsl-function:${Versions.f2}")
}
