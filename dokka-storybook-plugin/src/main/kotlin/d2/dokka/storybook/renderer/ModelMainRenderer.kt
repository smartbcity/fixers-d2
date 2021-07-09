package d2.dokka.storybook.renderer

import d2.dokka.storybook.builder.ReactFileBuilder
import d2.dokka.storybook.model.code.BasicImportedElement
import d2.dokka.storybook.model.code.imports.CodeImport
import d2.dokka.storybook.model.code.react.BasicComponent
import d2.dokka.storybook.model.code.react.CodeHighlighterComponent
import d2.dokka.storybook.model.code.react.DescriptedCodeComponent
import d2.dokka.storybook.model.page.FileData
import org.jetbrains.dokka.base.resolvers.local.LocationProvider
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage

open class ModelMainRenderer: D2ContentRenderer {

    override lateinit var d2LocationProvider: LocationProvider

    override fun buildPageContent(context: StringBuilder, page: ContentPage) {
        val builder = ReactFileBuilder(context)
        builder.buildFileHeader(page)
        builder.buildContentNode(page.content, page)
        builder.build()
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

    open fun ReactFileBuilder.buildGroup(node: ContentGroup, pageContext: ContentPage) {
        when (node.dci.kind) {
            ContentKind.Main -> node.children.forEach { child -> buildContentNode(child, pageContext) }
            ContentKind.Source -> buildDescriptedCodeComponent(node, pageContext)
            ContentKind.Extensions -> importExtensions(node, pageContext)
            else -> TODO()
        }
    }

    open fun ReactFileBuilder.buildDescriptedCodeComponent(node: ContentGroup, pageContext: ContentPage) {
        val descriptionImport = buildLocalImport(node, FileData.DESCRIPTION)
        val sampleImport = buildLocalImport(node, FileData.SAMPLE)

        val description = BasicComponent(importData = descriptionImport)
        val sample = BasicImportedElement(importData = sampleImport)

        val component = DescriptedCodeComponent(
            leftElement = description,
            rightElement = CodeHighlighterComponent(displayed = sample, language = "json", title = "Example")
        )
        append(component)
    }

    open fun ReactFileBuilder.importExtensions(node: ContentGroup, pageContext: ContentPage) {
        node.dci.dri.forEach { childDri ->
            val fullDri = childDri.copy(extra = FileData.MAIN.id)
            val path = d2LocationProvider.resolve(fullDri, node.sourceSets, pageContext)!!
            val import = buildImport(fullDri, FileData.MAIN, path)
            val component = BasicComponent(
                identifier = import.element,
                importData = import
            )
            appendNewLine()
            appendNewLine()
            append(component)
        }
    }


    open fun ReactFileBuilder.buildLocalImport(node: ContentNode, fileData: FileData): CodeImport {
        return buildImport(node.dci.dri.first(), fileData, "./$fileData")
    }

    open fun ReactFileBuilder.buildImport(target: DRI, fileData: FileData, path: String): CodeImport {
        val nodeId = target.classNames?.capitalize() ?: ""
        val elementId = fileData.id.capitalize()
        val elementName = "$nodeId$elementId"
        return CodeImport(
            path = path,
            element = elementName
        )
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