package d2.dokka.storybook.renderer

import d2.dokka.storybook.model.render.Article
import org.jetbrains.dokka.model.withDescendants
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentTable
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

class DescriptionPageContentRenderer(context: DokkaContext): MarkdownRenderer(context) {

    override fun StringBuilder.buildTableProperties(node: ContentTable, pageContext: ContentPage) {
        node.children.forEach { property ->
            wrapWith(Article) {
                append("\n")
                append("\n")
                property.children.forEach { child ->
                    val trailingChar = if (child is ContentGroup)
                        if (child.withDescendants().firstIsInstanceOrNull<ContentText>() == null) "\n\n" else ""
                    else " "
                    append(buildString { child.build(this, pageContext) } + trailingChar)
                }
            }
            append("\n")
        }
    }
}