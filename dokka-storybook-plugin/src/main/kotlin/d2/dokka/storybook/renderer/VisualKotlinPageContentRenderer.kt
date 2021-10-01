package d2.dokka.storybook.renderer

import org.jetbrains.dokka.model.firstChildOfType
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.pages.Style

class VisualKotlinPageContentRenderer: VisualPageContentRenderer() {

    override val VISUAL_TYPE = "Kotlin"
    private var indentLevel = 0

    override fun StringBuilder.buildProperties(node: ContentGroup, pageContext: ContentPage) {
        val header = node.children.first()
        val properties = node.children.drop(1)

        val className = header.firstChildOfType<ContentText>().text
        append("$className(")
        indentLevel++
        append(properties.map { it as ContentGroup }.joinToString(",") {
            val builder = StringBuilder()
            builder.appendNewLine()
            builder.buildContentNode(it.children[0], pageContext)
            builder.append(" = ")
            builder.decorateWith(it.style) {
                buildContentNode(it.children[1], pageContext)
            }
            builder
        })
        indentLevel--
        appendNewLine()
        append(")")
    }

    override fun Style.decorators(): Pair<String, String>? {
        return when (this) {
            ContentStyle.TabbedContent -> "listOf(" to ")"
            else -> null
        }
    }

//    override fun StringBuilder.beautify() {
//        val params = KtLint.Params(
//            text = this.toString(),
//            ruleSets = listOf(StandardRuleSetProvider().get()),
//            cb = { _, _ ->  }
//        )
//        val result = KtLint.format(params)
//        clear()
//        append(result)
//    }

    private fun StringBuilder.appendNewLine() {
        append("\n${"    ".repeat(indentLevel)}")
    }
}