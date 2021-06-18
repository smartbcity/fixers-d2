package d2.dokka.storybook

import org.jetbrains.dokka.gfm.renderer.CommonmarkRenderer
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.pages.ContentDRILink
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.query

class D2StorybookRenderer(context: DokkaContext): CommonmarkRenderer(context) {

    override val preprocessors = context.plugin<D2StorybookPlugin>().query { storybookPreprocessors }

    override fun StringBuilder.buildLink(address: String, content: StringBuilder.() -> Unit) {
        append(" link ")
    }

    override fun StringBuilder.buildDRILink(
        node: ContentDRILink,
        pageContext: ContentPage,
        sourceSetRestriction: Set<DisplaySourceSet>?
    ) {
        append(" DRILink ")
    }
}