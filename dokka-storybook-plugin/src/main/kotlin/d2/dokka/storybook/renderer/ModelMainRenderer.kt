package d2.dokka.storybook.renderer

import d2.dokka.storybook.builder.ReactFileBuilder
import d2.dokka.storybook.model.component.DescriptedCodeComponent
import d2.dokka.storybook.model.page.FileData
import d2.dokka.storybook.model.render.CodeImport
import org.jetbrains.dokka.base.resolvers.local.LocationProvider
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage

open class ModelMainRenderer: D2ContentRenderer {

    override lateinit var d2LocationProvider: LocationProvider

    protected open lateinit var builder: ReactFileBuilder

    override fun buildPageContent(context: StringBuilder, page: ContentPage) {
        builder = ReactFileBuilder(context)
        builder.buildFileHeader(page)
        page.content.build(page)
        builder.build()
    }

    open fun ContentNode.build(pageContext: ContentPage) {
        builder.buildContentNode(this, pageContext)
    }

    open fun ReactFileBuilder.buildContentNode(
        node: ContentNode,
        pageContext: ContentPage
    ) {
        when (node) {
            is ContentGroup -> buildGroup(node, pageContext)
            else -> TODO()
//            is ContentText -> buildText(node)
//            is ContentHeader -> buildHeader(node, pageContext, sourceSetRestriction)
//            is ContentCodeBlock -> buildCodeBlock(node, pageContext)
//            is ContentCodeInline -> buildCodeInline(node, pageContext)
//            is ContentDRILink -> buildDRILink(node, pageContext, sourceSetRestriction)
//            is ContentResolvedLink -> buildResolvedLink(node, pageContext, sourceSetRestriction)
//            is ContentEmbeddedResource -> buildResource(node, pageContext)
//            is ContentList -> buildList(node, pageContext, sourceSetRestriction)
//            is ContentTable -> buildTable(node, pageContext, sourceSetRestriction)
//            is ContentBreakLine -> buildNewLine()
//            is PlatformHintedContent -> buildPlatformDependent(node, pageContext, sourceSetRestriction)
//            is ContentDivergentGroup -> buildDivergent(node, pageContext)
//            is ContentDivergentInstance -> buildDivergentInstance(node, pageContext)
//            else -> buildError(node)
        }
    }

    open fun ReactFileBuilder.buildGroup(node: ContentNode, pageContext: ContentPage) {
        when (node.dci.kind) {
            ContentKind.Main -> buildDescriptedCodeComponent(node, pageContext)
            else -> TODO()
        }
    }

    open fun ReactFileBuilder.buildDescriptedCodeComponent(node: ContentNode, pageContext: ContentPage) {
        val descriptionComponent = buildLocalImport(node, FileData.DESCRIPTION)
        val codeComponent = buildLocalImport(node, FileData.SAMPLE)

        val component = DescriptedCodeComponent(
            description = "<$descriptionComponent />",
            code = "<$codeComponent />"
        )
        appendComponent(component)
    }

    open fun ReactFileBuilder.buildLocalImport(node: ContentNode, fileData: FileData): String {
        val nodeId = node.dci.dri.first().classNames?.capitalize() ?: ""
        val elementId = fileData.id.capitalize()
        val elementName = "$nodeId$elementId"
        val import = CodeImport(
            path = "./$fileData",
            element = elementName
        )
        addImport(import)
        return elementName
    }

    open fun ReactFileBuilder.buildFileHeader(pageContext: ContentPage) {
        val name = pageContext.dri.first().classNames ?: "Unknown"
        addImport(CodeImport(path = "@storybook/addon-docs/blocks", element = "Meta", isComposite = true))
        write {
            append("<Meta title=\"$name\" parameters={{ previewTabs: { canvas: { hidden: true } } }} />")
            append("\n\n")
        }
    }
}