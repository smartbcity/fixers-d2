plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
	kotlin("jvm")
	id("com.gradle.plugin-publish")
	`maven-publish`
	signing
}

repositories {
	gradlePluginPortal()
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${PluginVersions.kotlin}")
	implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:${PluginVersions.kotlin}")

	implementation("org.jetbrains.dokka:dokka-gradle-plugin:${PluginVersions.dokka}")

	implementation("city.smartb.fixers:config:${PluginVersions.fixers}")
}

pluginBundle {
	website = "https://smartb.city"
	vcsUrl = "https://github.com/smartbcity/d2"
	tags = listOf("SmartB", "Fixers", "kotlin", "dokka", "jvm")
}

gradlePlugin {
	plugins {
		create("city.smartb.fixers.gradle.d2") {
			id = "city.smartb.fixers.gradle.d2"
			implementationClass = "city.smartb.d2.fixers.gradle.D2Plugin"
			displayName = "Fixers Gradle d2"
			description = "Ease the configuration of d2 in order to generate documentation for storybook."
		}
	}
}

apply(from = rootProject.file("gradle/publishing.gradle"))
