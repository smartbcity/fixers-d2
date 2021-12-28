package city.smartb.d2.fixers.gradle

import city.smartb.gradle.config.fixers
import city.smartb.gradle.config.model.getD2
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register

class D2Plugin : Plugin<Project> {

	companion object {
		const val DOKKA_STORYBOOK = "dokkaStorybook"
		const val DOKKA_STORYBOOK_PARTIAL = "${DOKKA_STORYBOOK}Partial"
	}

	override fun apply(target: Project) {
		target.plugins.apply("org.jetbrains.dokka")
		target.subprojects {
			tasks {
				register<org.jetbrains.dokka.gradle.DokkaTask>(DOKKA_STORYBOOK_PARTIAL) {
					dependencies {
						plugins("city.smartb.d2:dokka-storybook-plugin:${project.version}")
					}
				}
			}
		}
		target.afterEvaluate {
			target.extensions.fixers?.let { config ->
				target.tasks {
					register<org.jetbrains.dokka.gradle.DokkaCollectorTask>(DOKKA_STORYBOOK) {
						addChildTask(DOKKA_STORYBOOK_PARTIAL)
						addSubprojectChildTasks(DOKKA_STORYBOOK_PARTIAL)
						outputDirectory.set(config.getD2().outputDirectory)
					}
				}
			}
		}
	}
}