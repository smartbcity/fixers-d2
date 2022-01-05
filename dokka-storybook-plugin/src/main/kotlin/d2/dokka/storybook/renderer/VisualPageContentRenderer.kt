package d2.dokka.storybook.renderer

import d2.dokka.storybook.location.D2StorybookLocationProvider
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.pages.ContentCodeBlock
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.pages.Style

abstract class VisualPageContentRenderer: D2ContentRenderer {

    protected abstract val visualType: String

    override lateinit var d2LocationProvider: D2StorybookLocationProvider

    override fun buildPageContent(context: StringBuilder, page: ContentPage) {
        page.content.build(context, page)
    }

    protected open fun ContentNode.build(
        builder: StringBuilder,
        pageContext: ContentPage,
        sourceSetRestriction: Set<DisplaySourceSet>? = null
    ) {
        builder.buildContentNode(this, pageContext, sourceSetRestriction)
        builder.beautify()
    }

    protected open fun StringBuilder.buildContentNode(
        node: ContentNode,
        pageContext: ContentPage,
        sourceSetRestriction: Set<DisplaySourceSet>? = null
    ) {
        if (sourceSetRestriction.isNullOrEmpty() || node.sourceSets.any { it in sourceSetRestriction }) {
            when (node) {
                is ContentCodeBlock -> buildCodeBlock(node, pageContext)
                is ContentGroup -> buildGroup(node, pageContext)
                is ContentText -> buildText(node, pageContext)
                else -> throw IllegalArgumentException("Cannot render content of type [${node::class.java}] in a Visual $visualType page")
            }
        }
    }

    protected open fun StringBuilder.buildGroup(node: ContentGroup, pageContext: ContentPage) {
        when (node.dci.kind) {
            ContentKind.Properties -> buildProperties(node, pageContext)
            ContentKind.Sample -> buildContentNode(node.children.first(), pageContext)
            else -> throw IllegalArgumentException("Cannot render ContentGroup of kind [${node.dci.kind}] in a Visual $visualType page")
        }
    }

    protected abstract fun StringBuilder.buildProperties(node: ContentGroup, pageContext: ContentPage)

    protected open fun StringBuilder.buildCodeBlock(node: ContentCodeBlock, pageContext: ContentPage) {
        buildContentNode(node.children.first(), pageContext)
    }

    protected open fun StringBuilder.buildText(node: ContentText, pageContext: ContentPage) {
        append(node.text)
    }

    protected open fun StringBuilder.beautify() {}

    protected open fun StringBuilder.decorateWith(styles: Set<Style>, block: StringBuilder.() -> Unit) {
        val (opens, closes) = styles.mapNotNull { it.decorators() }.unzip()
        opens.forEach(::append)
        block()
        closes.reversed().forEach(::append)
    }

    protected abstract fun Style.decorators(): Pair<String, String>?
}
