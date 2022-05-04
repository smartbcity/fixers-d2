import city.smartb.d2.fixers.gradle.D2
import city.smartb.gradle.config.ConfigExtension
import org.gradle.api.Action

fun ConfigExtension.d2(configure: Action<D2>) {
	configure.execute(getD2())
}

fun ConfigExtension.getD2(): D2 {
	return properties.getOrPut("d2") { D2(outputDirectory = project.file("storybook/stories/d2")) } as D2
}
