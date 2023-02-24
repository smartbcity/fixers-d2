package d2.dokka.storybook.renderer.visual

import d2.dokka.storybook.model.code.react.LiteralNode
import d2.dokka.storybook.model.code.react.g2.AutomateViewerComponent
import d2.dokka.storybook.renderer.builder.ReactFileBuilder
import org.jetbrains.dokka.model.withDescendants
import org.jetbrains.dokka.pages.ContentCodeBlock
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.pages.Style
import java.io.File

class VisualAutomatePageContentRenderer: VisualPageContentRenderer() {

    override val visualType = "Automate"

    override fun buildPageContent(context: StringBuilder, page: ContentPage) {
        val builder = ReactFileBuilder(context)

        val path = page.content.withDescendants().filterIsInstance<ContentText>().first().text
        builder.append(AutomateViewerComponent(automate = LiteralNode(File(path).readText())))

        builder.build()
    }

    override fun StringBuilder.buildCodeBlock(node: ContentCodeBlock, pageContext: ContentPage) {
        buildContentNode(node.children.first(), pageContext)
    }

    override fun StringBuilder.buildProperties(node: ContentGroup, pageContext: ContentPage) {}
    override fun Style.decorators(): Pair<String, String>? { return null }
}
