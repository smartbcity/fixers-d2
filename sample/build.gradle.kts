plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation("city.smartb.f2:f2-dsl-cqrs:${Versions.f2}")
    implementation("city.smartb.f2:f2-dsl-function:${Versions.f2}")
}

tasks {
    create<org.jetbrains.dokka.gradle.DokkaTask>("dokkaD2") {
        dependencies {
            plugins(project(":dokka-storybook-plugin"))
        }
    }
}
