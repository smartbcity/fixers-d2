package d2.dokka.storybook.renderer

import d2.dokka.storybook.location.D2StorybookLocationProvider
import d2.dokka.storybook.model.render.D2TextStyle
import d2.dokka.storybook.model.render.WrapperTag
import org.jetbrains.dokka.gfm.renderer.CommonmarkRenderer
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.pages.ContentDRILink
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentList
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentTable
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.pages.Style
import org.jetbrains.dokka.pages.TextStyle
import org.jetbrains.dokka.plugability.DokkaContext

open class MarkdownRenderer(
    context: DokkaContext,
): CommonmarkRenderer(context), D2ContentRenderer {

    override lateinit var d2LocationProvider: D2StorybookLocationProvider

    override fun buildPageContent(context: StringBuilder, page: ContentPage) {
        page.content.build(context, page)
    }

    override fun StringBuilder.buildTable(
        node: ContentTable,
        pageContext: ContentPage,
        sourceSetRestriction: Set<DisplaySourceSet>?
    ) {
        localbuildNewLine()
        when (node.dci.kind) {
            ContentKind.Properties -> buildTableProperties(node, pageContext)
            ContentKind.Sample,
            ContentKind.Parameters -> buildTableSampleOrParameters(node, pageContext, sourceSetRestriction)
            else -> buildTableDefault(node, pageContext)
        }
    }

    fun StringBuilder.localbuildNewLine() {
        append("  \n")
    }

    protected open fun StringBuilder.buildTableProperties(node: ContentTable, pageContext: ContentPage) {
        buildTableDefault(node, pageContext)
    }

    protected open fun StringBuilder.buildTableSampleOrParameters(node: ContentTable, pageContext: ContentPage, sourceSetRestriction: Set<DisplaySourceSet>?) {
        node.sourceSets.forEach { sourceSetData ->
            append(sourceSetData.name)
            localbuildNewLine()
            buildTable(
                node.copy(
                    children = node.children.filter { it.sourceSets.contains(sourceSetData) },
                    dci = node.dci.copy(kind = ContentKind.Main)
                ), pageContext, sourceSetRestriction
            )
            localbuildNewLine()
        }
    }

    protected open fun StringBuilder.buildTableDefault(node: ContentTable, pageContext: ContentPage) {
        val size = node.header.firstOrNull()?.children?.size ?: node.children.firstOrNull()?.children?.size ?: 0

        if (node.header.isNotEmpty()) {
            node.header.forEach {
                append("| ")
                it.children.forEach {
                    append(" ")
                    it.build(this, pageContext, it.sourceSets)
                    append(" | ")
                }
                append("\n")
            }
        } else {
            append("| ".repeat(size))
            if (size > 0) append("|\n")
        }

        append("|---".repeat(size))
        if (size > 0) append("|\n")

        node.children.forEach {
            val builder = StringBuilder()
            it.children.forEach {
                builder.append("| ")
                builder.append("<a name=\"${it.dci.dri.first()}\"></a>")
                builder.append(
                    buildString { it.build(this, pageContext) }.replace(
                        Regex("#+ "),
                        ""
                    )
                )  // Workaround for headers inside tables
            }
            append(builder.toString().withEntersAsHtml())
            append("|".repeat(size + 1 - it.children.size))
            append("\n")
        }
    }

    override fun StringBuilder.buildText(textNode: ContentText) {
        if (textNode.text.isNotBlank()) {
            val decorators = decorators(textNode.style)
            append(textNode.text.takeWhile { it == ' ' })
            append(decorators)
            append(textNode.text.trim())
            append(decorators.reversed())
            append(textNode.text.takeLastWhile { it == ' ' })
        }
    }

    protected open fun StringBuilder.wrapWith(tag: WrapperTag, buildContent: StringBuilder.() -> Unit) {
        wrapWith(listOf(tag), buildContent)
    }

    protected open fun StringBuilder.wrapWith(tags: List<WrapperTag>, buildContent: StringBuilder.() -> Unit) {
        tags.forEach { tag -> append(tag.open()) }
        buildContent()
        tags.forEach { tag -> append(tag.close()) }
    }

    protected open fun decorators(styles: Set<Style>) = buildString {
        styles.forEach { style ->
            when (style) {
                TextStyle.Bold -> append("**")
                TextStyle.Italic -> append("*")
                TextStyle.Strong -> append("**")
                TextStyle.Strikethrough -> append("~~")
                D2TextStyle.Code -> append("`")
                else -> Unit
            }
        }
    }

    protected open fun String.withEntersAsHtml(): String = this
        .replace("\\\n", "\n\n")
        .replace("\n[\n]+".toRegex(), "<br>")
        .replace("\n", " ")

    override fun StringBuilder.buildDRILink(
        node: ContentDRILink,
        pageContext: ContentPage,
        sourceSetRestriction: Set<DisplaySourceSet>?
    ) {
        d2LocationProvider.resolveAnchor(node.address, pageContext)?.let {
            buildLink(it) {
                buildText(node.children, pageContext, sourceSetRestriction)
            }
        } ?: buildText(node.children, pageContext, sourceSetRestriction)
    }

    override fun StringBuilder.buildList(
        node: ContentList,
        pageContext: ContentPage,
        sourceSetRestriction: Set<DisplaySourceSet>?
    ) {
        node.children.forEach { child ->
            append(" - ")
            append(buildString { child.build(this, pageContext, sourceSetRestriction) }.trim())
            append("\n")
        }
    }
}
