package d2.dokka.storybook.renderer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.dokka.base.resolvers.local.LocationProvider
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentText

open class ModelSampleRenderer: D2ContentRenderer {

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
//                is ContentGroup -> buildGroup(node, pageContext, sourceSetRestriction)
                is ContentGroup -> {
                    append("{")
                    append(node.children.map { it as ContentGroup }.joinToString(",") {
                        val builder = StringBuilder()
                        builder.append((it.children[0] as ContentText).text)
                        builder.append(":")
                        builder.append((it.children[1] as ContentText).text.replace("\\", ""))
                    })
                    append("}")
                }
                else -> TODO()
//                is ContentText -> buildText(node)
//                is ContentHeader -> buildHeader(node, pageContext, sourceSetRestriction)
//                is ContentCodeBlock -> buildCodeBlock(node, pageContext)
//                is ContentCodeInline -> buildCodeInline(node, pageContext)
//                is ContentDRILink -> buildDRILink(node, pageContext, sourceSetRestriction)
//                is ContentResolvedLink -> buildResolvedLink(node, pageContext, sourceSetRestriction)
//                is ContentEmbeddedResource -> buildResource(node, pageContext)
//                is ContentList -> buildList(node, pageContext, sourceSetRestriction)
//                is ContentTable -> buildTable(node, pageContext, sourceSetRestriction)
//                is ContentBreakLine -> buildNewLine()
//                is PlatformHintedContent -> buildPlatformDependent(node, pageContext, sourceSetRestriction)
//                is ContentDivergentGroup -> buildDivergent(node, pageContext)
//                is ContentDivergentInstance -> buildDivergentInstance(node, pageContext)
//                else -> buildError(node)
            }
        }
    }

    protected open fun buildJson() {
//        ObjectMapper
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