plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly("org.jetbrains.dokka:dokka-core:${PluginVersions.dokka}"){
//        exclude(group = "org.jetbrains.kotlin", module = "kotlin-compiler-embeddable")
    }
    implementation("org.jetbrains.dokka:dokka-base:${PluginVersions.dokka}")
    implementation("org.jetbrains.dokka:gfm-plugin:${PluginVersions.dokka}")
    implementation(kotlin("stdlib"))

    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}")
//    runtimeOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.32")
//    implementation("com.pinterest.ktlint:ktlint-core:${Versions.ktlint}") {
//        exclude(group = "org.jetbrains.kotlin", module = "kotlin-compiler-embeddable")
//    }
//    implementation("com.pinterest.ktlint:ktlint-ruleset-standard:${Versions.ktlint}") {
////        exclude(group = "org.jetbrains.kotlin", module = "kotlin-compiler-embeddable")
//    }


    testImplementation(kotlin("test-junit"))
    testImplementation("org.jetbrains.dokka:dokka-test-api:${PluginVersions.dokka}")
}

apply(from = rootProject.file("gradle/publishing.gradle"))
