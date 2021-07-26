package d2.dokka.storybook.renderer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.dokka.base.resolvers.local.LocationProvider
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.pages.ContentCodeBlock
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentText

open class SamplePageContentRenderer: D2ContentRenderer {

    override lateinit var d2LocationProvider: LocationProvider

    override fun buildPageContent(context: StringBuilder, page: ContentPage) {
        page.content.build(context, page)
    }

    open fun ContentNode.build(
        builder: StringBuilder,
        pageContext: ContentPage,
        sourceSetRestriction: Set<DisplaySourceSet>? = null
    ) {
        builder.buildContentNode(this, pageContext, sourceSetRestriction)
        builder.beautifyJson()
    }

    open fun StringBuilder.buildContentNode(
        node: ContentNode,
        pageContext: ContentPage,
        sourceSetRestriction: Set<DisplaySourceSet>? = null
    ) {
        if (sourceSetRestriction.isNullOrEmpty() || node.sourceSets.any { it in sourceSetRestriction }) {
            when (node) {
                is ContentGroup -> buildGroup(node, pageContext)
                is ContentCodeBlock -> buildCodeBlock(node, pageContext)
                is ContentText -> buildText(node, pageContext)
                else -> throw IllegalArgumentException("Cannot render content of type [${node::class.java}] in a Sample page")
            }
        }
    }

    protected open fun StringBuilder.buildGroup(node: ContentGroup, pageContext: ContentPage) {
        when (node.dci.kind) {
            ContentKind.Properties -> buildProperties(node, pageContext)
            ContentKind.Sample -> buildContentNode(node.children.first(), pageContext)
            else -> throw IllegalArgumentException("Cannot render ContentGroup of kind [${node.dci.kind}] in a Sample page")
        }
    }

    protected open fun StringBuilder.buildProperties(node: ContentGroup, pageContext: ContentPage) {
        append("{")
        append(node.children.map { it as ContentGroup }.joinToString(",") {
            val builder = StringBuilder()
            builder.append((it.children[0] as ContentText).text)
            builder.append(":")
            builder.append((it.children[1] as ContentText).text)
        })
        append("}")
    }

    protected open fun StringBuilder.buildCodeBlock(node: ContentCodeBlock, pageContext: ContentPage) {
        buildContentNode(node.children.first(), pageContext)
    }

    protected open fun StringBuilder.buildText(node: ContentText, pageContext: ContentPage) {
        append(node.text)
    }

    protected open fun StringBuilder.beautifyJson() {
        val prettyJson = toPrettyJson()
        clear()
        append(prettyJson)
    }

    protected open fun StringBuilder.toPrettyJson(): String {
        val mapper = ObjectMapper()
            .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
        val json = mapper.readValue(this.toString(), Any::class.java)
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json)
    }
}