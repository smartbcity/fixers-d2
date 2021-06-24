package d2.dokka.storybook.renderer

import org.jetbrains.dokka.base.resolvers.local.LocationProvider
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.plugability.DokkaContext

open class ModelSampleRenderer(context: DokkaContext): D2ContentRenderer {

    override lateinit var d2LocationProvider: LocationProvider

    override fun buildPageContent(context: StringBuilder, page: ContentPage) {
        page.content.build(context, page)
    }

    open fun ContentNode.build(
        builder: StringBuilder,
        pageContext: ContentPage,
        sourceSetRestriction: Set<DisplaySourceSet>? = null
    ) =
        builder.buildContentNode(this, pageContext, sourceSetRestriction)

    open fun StringBuilder.buildContentNode(
        node: ContentNode,
        pageContext: ContentPage,
        sourceSetRestriction: Set<DisplaySourceSet>? = null
    ) {
        if (sourceSetRestriction.isNullOrEmpty() || node.sourceSets.any { it in sourceSetRestriction }) {
            when (node) {
                else -> append(node::class.simpleName + "\n")
//                is ContentText -> buildText(node)
//                is ContentHeader -> buildHeader(node, pageContext, sourceSetRestriction)
//                is ContentCodeBlock -> buildCodeBlock(node, pageContext)
//                is ContentCodeInline -> buildCodeInline(node, pageContext)
//                is ContentDRILink -> buildDRILink(node, pageContext, sourceSetRestriction)
//                is ContentResolvedLink -> buildResolvedLink(node, pageContext, sourceSetRestriction)
//                is ContentEmbeddedResource -> buildResource(node, pageContext)
//                is ContentList -> buildList(node, pageContext, sourceSetRestriction)
//                is ContentTable -> buildTable(node, pageContext, sourceSetRestriction)
//                is ContentGroup -> buildGroup(node, pageContext, sourceSetRestriction)
//                is ContentBreakLine -> buildNewLine()
//                is PlatformHintedContent -> buildPlatformDependent(node, pageContext, sourceSetRestriction)
//                is ContentDivergentGroup -> buildDivergent(node, pageContext)
//                is ContentDivergentInstance -> buildDivergentInstance(node, pageContext)
//                else -> buildError(node)
            }
        }
    }

    protected open fun buildJson() {

    }
}