plugins {
    id("city.smartb.fixers.gradle.kotlin.jvm")
    kotlin("plugin.jpa")
}

dependencies {
    implementation("city.smartb.f2:f2-dsl-cqrs:0.8.0")
    implementation("city.smartb.f2:f2-dsl-function:0.8.0")
    implementation("city.smartb.f2:f2-spring-boot-starter-function-http:0.8.0")
}
