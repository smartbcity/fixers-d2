plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
	id("com.gradle.plugin-publish")
}

repositories {
	gradlePluginPortal()
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${PluginVersions.kotlin}")
	implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:${PluginVersions.kotlin}")

	implementation("org.jetbrains.dokka:dokka-gradle-plugin:${PluginVersions.dokka}")

	implementation(project(":dokka-storybook-plugin"))
	implementation("city.smartb.fixers.gradle:config:${PluginVersions.fixers}")
	implementation("city.smartb.fixers.gradle:plugin:${PluginVersions.fixers}")
}

gradlePlugin {
	website = "https://smartb.city"
	vcsUrl = "https://github.com/smartbcity/d2"
	plugins {
		create("city.smartb.fixers.gradle.d2") {
			id = "city.smartb.fixers.gradle.d2"
			implementationClass = "city.smartb.d2.fixers.gradle.D2Plugin"
			displayName = "Fixers Gradle d2"
			description = "Ease the configuration of d2 in order to generate documentation for storybook."
			tags = listOf("SmartB", "Fixers", "kotlin", "dokka", "d2")
		}
	}
}

apply(from = rootProject.file("gradle/publishing_plugin.gradle"))
