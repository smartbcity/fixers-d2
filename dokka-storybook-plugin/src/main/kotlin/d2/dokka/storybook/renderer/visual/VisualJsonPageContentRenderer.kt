package d2.dokka.storybook.renderer.visual

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.Style

class VisualJsonPageContentRenderer: VisualPageContentRenderer() {

    override val visualType = "Json"

    override fun StringBuilder.buildProperties(node: ContentGroup, pageContext: ContentPage) {
        val header = node.children.first()
        val properties = node.children.drop(1)

        append("{")
        append(properties.map { it as ContentGroup }.joinToString(",") {
            val builder = StringBuilder()
            builder.buildContentNode(it.children[0], pageContext)
            builder.append(":")
            builder.decorateWith(it.style) {
                buildContentNode(it.children[1], pageContext)
            }
            builder
        })
        append("}")
    }

    override fun Style.decorators(): Pair<String, String>? {
        return when (this) {
            ContentStyle.TabbedContent -> "[" to "]"
            else -> null
        }
    }

    override fun StringBuilder.beautify() {
        val prettyJson = toPrettyJson()
        clear()
        append(prettyJson)
    }

    private fun StringBuilder.toPrettyJson(): String {
        val mapper = ObjectMapper()
            .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
        val json = mapper.readValue(this.toString(), Any::class.java)
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json)
    }
}
