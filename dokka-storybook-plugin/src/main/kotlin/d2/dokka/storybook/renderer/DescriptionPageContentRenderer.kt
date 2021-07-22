package d2.dokka.storybook.renderer

import d2.dokka.storybook.model.render.Article
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentTable
import org.jetbrains.dokka.plugability.DokkaContext

class DescriptionPageContentRenderer(context: DokkaContext): MarkdownRenderer(context) {

    override fun StringBuilder.buildTableProperties(node: ContentTable, pageContext: ContentPage) {
        node.children.forEach { property ->
            wrapWith(Article) {
                append("\n")
                append("\n")
                property.children.forEach { child ->
                    val trailingSpace = if (child is ContentGroup) "" else " "
                    append(buildString { child.build(this, pageContext) } + trailingSpace)
                }
            }
            append("\n")
        }
    }
}