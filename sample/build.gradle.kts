plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(project(":d2-dsl"))
}

tasks {
    create<org.jetbrains.dokka.gradle.DokkaTask>("dokkaD2") {
        dependencies {
            plugins(project(":dokka-storybook-plugin"))
        }
    }
}
