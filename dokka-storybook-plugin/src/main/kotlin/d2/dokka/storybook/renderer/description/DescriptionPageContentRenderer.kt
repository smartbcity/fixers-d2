package d2.dokka.storybook.renderer.description

import d2.dokka.storybook.model.code.react.LiteralNode
import d2.dokka.storybook.model.code.react.g2.DocsTableComponent
import d2.dokka.storybook.renderer.MarkdownRenderer
import d2.dokka.storybook.renderer.builder.ReactFileBuilder
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentTable
import org.jetbrains.dokka.plugability.DokkaContext

class DescriptionPageContentRenderer(context: DokkaContext): MarkdownRenderer(context) {

    override fun ReactFileBuilder.buildTableProperties(node: ContentTable, pageContext: ContentPage) {
        node.children.forEach { property ->
            appendDivider()
            property.children.forEach { child ->
                append(buildString { child.build(ReactFileBuilder(this), pageContext) })
            }
        }
    }

    override fun ReactFileBuilder.buildTableFunctions(node: ContentTable, pageContext: ContentPage) {
        append(DocsTableComponent(
            blocks = node.children.flatMap { function ->
                function.children.map { block ->
                    LiteralNode(buildString { block.build(ReactFileBuilder(this), pageContext) })
                }
            }
        ))
    }
}
