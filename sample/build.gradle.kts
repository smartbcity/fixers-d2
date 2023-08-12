import city.smartb.gradle.dependencies.FixersDependencies

plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
}

dependencies {
    FixersDependencies.Jvm.Kotlin.coroutines(::implementation)
    implementation("city.smartb.f2:f2-dsl-cqrs:0.8.0")
    implementation("city.smartb.f2:f2-dsl-function:0.8.0")
    implementation("city.smartb.f2:f2-spring-boot-starter-function-http:0.8.0")
}
