plugins {
    kotlin("plugin.jpa") version PluginVersions.kotlin apply false
    kotlin("kapt") version PluginVersions.kotlin apply false

    id("org.jetbrains.dokka") version PluginVersions.dokka
    id("com.gradle.plugin-publish") version PluginVersions.gradlePublish apply false

    id("city.smartb.fixers.gradle.config") version PluginVersions.fixers
    id("city.smartb.fixers.gradle.sonar") version PluginVersions.fixers
}

allprojects {
    group = "city.smartb.d2"
    version = System.getenv("VERSION") ?: "local"
    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://oss.sonatype.org/service/local/repositories/releases/content") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    }
}

fixers {
    bundle {
        id = "d2"
        name = "D2"
        description = "Dokka plugin for SmartB projects"
        url = "https://gitlab.smartb.city/fixers/d2"
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
