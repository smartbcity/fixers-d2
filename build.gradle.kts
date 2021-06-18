import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") version PluginVersions.kotlin apply false
    kotlin("jvm") version PluginVersions.kotlin apply false
    id("org.jetbrains.dokka") version PluginVersions.dokka
}

allprojects {
    group = "city.smartb.d2"
    version = System.getenv("VERSION") ?: "local"
    repositories {
        mavenLocal()
        jcenter()
        mavenCentral()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven("https://repo.spring.io/snapshot")
        maven("https://repo.spring.io/milestone")
    }
}

subprojects {
    plugins.withType(org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper::class.java).whenPluginAdded {
        the<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>().apply {
            jvm {
                compilations.all {
                    kotlinOptions.jvmTarget = "11"
                }
            }
            js(IR) {
                binaries.library()
                browser {
                    browser()
                    binaries.executable()

                    testTask {
                        useKarma {
                            useChromeHeadless()
                        }
                    }
                }
            }
            sourceSets {
                val commonMain by getting {
                    dependencies {
                        implementation(kotlin("reflect"))
                    }
                }
                val commonTest by getting {
                    dependencies {
                        implementation(kotlin("test-common"))
                        implementation(kotlin("test-annotations-common"))
                    }
                }
                val jvmMain by getting
                val jvmTest by getting {
                    dependencies {
                        implementation(kotlin("reflect"))
                    }
                }
                val jsMain by getting {
                    dependencies {
                    }
                }
                val jsTest by getting {
                    dependencies {
                        implementation(kotlin("test-js"))
                    }
                }
            }
        }
    }
    plugins.withType(JavaPlugin::class.java).whenPluginAdded {
        tasks.withType<KotlinCompile>().configureEach {
            println("Configuring $name in project ${project.name}...")
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "11"
            }
        }
        tasks.withType<JavaCompile> {
            sourceCompatibility = JavaVersion.VERSION_11.toString()
            targetCompatibility = JavaVersion.VERSION_11.toString()
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }

        dependencies {
            val implementation by configurations
            implementation(kotlin("reflect"))
        }
    }
}
