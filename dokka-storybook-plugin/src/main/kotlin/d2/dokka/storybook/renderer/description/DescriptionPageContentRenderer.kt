package d2.dokka.storybook.renderer.description

import d2.dokka.storybook.model.render.Article
import d2.dokka.storybook.renderer.MarkdownRenderer
import d2.dokka.storybook.renderer.builder.ReactFileBuilder
import org.jetbrains.dokka.model.withDescendants
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentTable
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.plugability.DokkaContext

class DescriptionPageContentRenderer(context: DokkaContext): MarkdownRenderer(context) {

    override fun ReactFileBuilder.buildTableProperties(node: ContentTable, pageContext: ContentPage) {
        node.children.forEach { property ->
            wrapWith(Article) {
                append("\n")
                append("\n")
                property.children.forEach { child ->
                    val trailingChar = if (child is ContentGroup) {
                        if (child.withDescendants().none { it is ContentText }) {
                            "\n\n"
                        } else {
                            ""
                        }
                    } else {
                        " "
                    }
                    append(buildString { child.build(ReactFileBuilder(this), pageContext) } + trailingChar)
                }
            }
            append("\n")
        }
    }
}
