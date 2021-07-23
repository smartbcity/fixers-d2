pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "d2"

include("dokka-storybook-plugin")
include("sample")
include("sample-fragment")
