plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("org.jetbrains.dokka:dokka-core:${PluginVersions.dokka}")
    implementation("org.jetbrains.dokka:dokka-base:${PluginVersions.dokka}")
    implementation("org.jetbrains.dokka:gfm-plugin:${PluginVersions.dokka}")
    implementation(project(":d2-dsl"))

    testImplementation(kotlin("test-junit"))
    testImplementation("org.jetbrains.dokka:dokka-test-api:${PluginVersions.dokka}")
}

apply(from = rootProject.file("gradle/publishing.gradle"))
