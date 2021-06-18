plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(project(":d2-dsl"))
}

tasks {
    named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaGfm") {
        dependencies {
            plugins(project(":dokka-storybook-plugin"))
        }
    }
}
