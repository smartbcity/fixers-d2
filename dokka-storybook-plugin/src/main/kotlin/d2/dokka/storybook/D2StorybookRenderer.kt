package d2.dokka.storybook

import d2.dokka.storybook.model.D2TextStyle
import d2.dokka.storybook.model.WrapperTag
import org.jetbrains.dokka.gfm.renderer.CommonmarkRenderer
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentTable
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.pages.Style
import org.jetbrains.dokka.pages.TextStyle
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.query

class D2StorybookRenderer(context: DokkaContext): CommonmarkRenderer(context) {

    override val preprocessors = context.plugin<D2StorybookPlugin>().query { storybookPreprocessors }

//    override fun StringBuilder.buildLink(address: String, content: StringBuilder.() -> Unit) {
//        append(" link ")
//    }
//
//    override fun StringBuilder.buildDRILink(
//        node: ContentDRILink,
//        pageContext: ContentPage,
//        sourceSetRestriction: Set<DisplaySourceSet>?
//    ) {
//        append(" DRILink ")
//    }

    override fun buildPageContent(context: StringBuilder, page: ContentPage) {
        page.content.build(context, page)
    }

    override fun StringBuilder.buildTable(
        node: ContentTable,
        pageContext: ContentPage,
        sourceSetRestriction: Set<DisplaySourceSet>?
    ) {
        buildNewLine()
        when (node.dci.kind) {
            ContentKind.Properties -> buildTableProperties(node, pageContext)
            ContentKind.Sample,
            ContentKind.Parameters -> buildTableSampleOrParameters(node, pageContext, sourceSetRestriction)
            else -> buildTableDefault(node, pageContext)
        }
    }

    private fun StringBuilder.buildTableProperties(node: ContentTable, pageContext: ContentPage) {
        node.children.forEach { child ->
            wrapWith(WrapperTag.Article) {
                buildNewLine()
                buildNewLine()
                child.children.forEach { subChild ->
                    val trailingSpace = if (subChild is ContentGroup) "" else " "
                    append(buildString { subChild.build(this, pageContext) } + trailingSpace)
                }
            }
            buildNewLine()
        }
    }

    private fun StringBuilder.buildTableSampleOrParameters(node: ContentTable, pageContext: ContentPage, sourceSetRestriction: Set<DisplaySourceSet>?) {
        node.sourceSets.forEach { sourceSetData ->
            append(sourceSetData.name)
            buildNewLine()
            buildTable(
                node.copy(
                    children = node.children.filter { it.sourceSets.contains(sourceSetData) },
                    dci = node.dci.copy(kind = ContentKind.Main)
                ), pageContext, sourceSetRestriction
            )
            buildNewLine()
        }
    }

    private fun StringBuilder.buildTableDefault(node: ContentTable, pageContext: ContentPage) {
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

    private fun StringBuilder.wrapWith(tag: WrapperTag, buildContent: StringBuilder.() -> Unit) {
        wrapWith(listOf(tag), buildContent)
    }

    private fun StringBuilder.wrapWith(tags: List<WrapperTag>, buildContent: StringBuilder.() -> Unit) {
        tags.forEach { tag -> append(tag.open()) }
        buildContent()
        tags.forEach { tag -> append(tag.close()) }
    }

    private fun decorators(styles: Set<Style>) = buildString {
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

    private fun String.withEntersAsHtml(): String = this
        .replace("\\\n", "\n\n")
        .replace("\n[\n]+".toRegex(), "<br>")
        .replace("\n", " ")
}