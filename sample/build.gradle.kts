plugins {
    id("city.smartb.fixers.gradle.kotlin.jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation("city.smartb.f2:f2-dsl-cqrs:0.2.4")
    implementation("city.smartb.f2:f2-dsl-function:0.2.4")
}
