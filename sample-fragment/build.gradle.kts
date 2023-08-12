import city.smartb.gradle.dependencies.FixersDependencies

plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        browser()
    }
    jvm {
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":sample"))
            }
        }
    }
}