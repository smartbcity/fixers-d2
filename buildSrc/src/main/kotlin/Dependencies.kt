import city.smartb.gradle.dependencies.FixersPluginVersions
import city.smartb.gradle.dependencies.FixersVersions
import org.gradle.kotlin.dsl.embeddedKotlinVersion

object PluginVersions {
	val fixers = FixersPluginVersions.fixers
	var kotlinDsl = embeddedKotlinVersion
	var kotlin = FixersPluginVersions.kotlin
	var dokka = "1.7.20"
	const val gradlePublish = "1.2.0"
}

object Versions {
	const val junit = FixersVersions.Test.junit
	const val jackson = FixersVersions.Json.jackson
}
