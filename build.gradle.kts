plugins {
    kotlin("plugin.jpa") version PluginVersions.kotlinDsl apply false
    kotlin("kapt") version PluginVersions.kotlinDsl apply false
    kotlin("jvm") version PluginVersions.kotlinDsl apply false
    kotlin("multiplatform") version PluginVersions.kotlinDsl apply false

    id("org.jetbrains.dokka") version PluginVersions.dokka
    id("com.gradle.plugin-publish") version PluginVersions.gradlePublish apply false
}

allprojects {
    group = "city.smartb.d2"
    version = System.getenv("VERSION") ?: "local"
    repositories {
        mavenCentral()
        maven { url = uri("https://oss.sonatype.org/service/local/repositories/releases/content") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        mavenLocal()
    }
}

val dokkaStorybook = "dokkaStorybook"
val dokkaStorybookPartial = "${dokkaStorybook}Partial"

subprojects {
    tasks {
        register<org.jetbrains.dokka.gradle.DokkaTask>(dokkaStorybookPartial) {
            dependencies {
                plugins(project(":dokka-storybook-plugin"))
            }
        }
    }
}

tasks {
    register<org.jetbrains.dokka.gradle.DokkaCollectorTask>(dokkaStorybook) {
        dependencies {
            plugins(project(":dokka-storybook-plugin"))
        }
        addChildTask(dokkaStorybookPartial)
        addSubprojectChildTasks(dokkaStorybookPartial)
        outputDirectory.set(file("storybook/docs/d2"))
    }
}
