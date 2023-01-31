import city.smartb.gradle.dependencies.FixersPluginVersions
import city.smartb.gradle.dependencies.FixersVersions

object PluginVersions {
	val fixers = FixersPluginVersions.fixers
	var kotlin = FixersPluginVersions.kotlin
	var dokka = "1.7.20"
	const val gradlePublish = "0.15.0"
}

object Versions {
	const val junit = FixersVersions.Test.junit
	const val jackson = FixersVersions.Json.jackson
}
