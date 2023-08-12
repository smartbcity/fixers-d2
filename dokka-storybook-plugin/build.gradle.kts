import city.smartb.gradle.dependencies.FixersDependencies

plugins {
    `kotlin-dsl`
    kotlin("jvm")
    id("com.gradle.plugin-publish")
}


dependencies {
    FixersDependencies.Jvm.Kotlin.coroutines(::implementation)
    compileOnly("org.jetbrains.dokka:dokka-core:${PluginVersions.dokka}")
    implementation("org.jetbrains.dokka:dokka-base:${PluginVersions.dokka}")
    implementation("org.jetbrains.dokka:gfm-plugin:${PluginVersions.dokka}")
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}")

    testImplementation(kotlin("test-junit"))
    testImplementation("org.jetbrains.dokka:dokka-test-api:${PluginVersions.dokka}")
}
