pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://oss.sonatype.org/service/local/repositories/releases/content") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        mavenLocal()
    }
}

rootProject.name = "d2"

include("dokka-storybook-plugin")
include("fixers-plugin")
include("sample")
include("sample-fragment")
