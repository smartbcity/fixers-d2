package d2.dokka.storybook.renderer

import d2.dokka.storybook.builder.ReactFileBuilder
import d2.dokka.storybook.model.code.BasicImportedElement
import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.imports.CodeImport
import d2.dokka.storybook.model.code.react.BasicComponent
import d2.dokka.storybook.model.code.react.CodeHighlighterComponent
import d2.dokka.storybook.model.code.react.DescriptedCodeComponent
import d2.dokka.storybook.model.page.FileData
import org.jetbrains.dokka.base.resolvers.local.LocationProvider
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.links.sureClassNames
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentText

open class MainPageContentRenderer: D2ContentRenderer {

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
            else -> throw IllegalArgumentException("Cannot render content of type [${node::class.java}] in a Main page")
        }
    }

    open fun ReactFileBuilder.buildGroup(node: ContentGroup, pageContext: ContentPage) {
        when (node.dci.kind) {
            ContentKind.Main -> node.children.forEach { child -> buildContentNode(child, pageContext) }
            ContentKind.Source -> importSources(node, pageContext)
            ContentKind.Extensions -> importExtensions(node, pageContext)
            ContentKind.Empty -> Unit
            else -> throw IllegalArgumentException("Cannot render ContentGroup of kind [${node.dci.kind}] in a Main page")
        }
    }

    open fun ReactFileBuilder.importSources(node: ContentGroup, pageContext: ContentPage) {
        when (node.children.size) {
            0 -> return
            1 -> buildOneColumnSources(node, pageContext)
            2 -> buildTwoColumnsSources(node, pageContext)
            else -> throw IllegalArgumentException("A page cannot have more than 2 columns ATM")
        }

    }

    open fun ReactFileBuilder.buildOneColumnSources(node: ContentGroup, pageContext: ContentPage) {
        val element = node.children.first() as ContentText
        append(element.toSourceComponent(node))
    }

    open fun ReactFileBuilder.buildTwoColumnsSources(node: ContentGroup, pageContext: ContentPage) {
        val (leftElement, rightElement) = node.children as List<ContentText>

        val component = DescriptedCodeComponent(
            leftElement = leftElement.toSourceComponent(node),
            rightElement = rightElement.toSourceComponent(node)
        )
        append(component)
    }

    private fun ContentText.toSourceComponent(parent: ContentGroup): CodeElement {
        val codeImport = buildLocalImport(parent, FileData.fromId(this.text))

        return when (this.dci.kind) {
            ContentKind.Comment -> BasicComponent(importData = codeImport)
            ContentKind.Sample -> {
                val sample = BasicImportedElement(importData = codeImport)
                CodeHighlighterComponent(displayed = sample, language = "json", title = "Example")
            }
            else -> throw IllegalArgumentException("Unsupported ContentKind[${this.dci.kind}] for source files")
        }
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


    open fun buildLocalImport(node: ContentNode, fileData: FileData): CodeImport {
        return buildImport(node.dci.dri.first(), fileData, "./$fileData")
    }

    open fun buildImport(target: DRI, fileData: FileData, path: String): CodeImport {
        val nodeId = target.sureClassNames.capitalize()
        val elementId = fileData.id.capitalize()
        val elementName = "$nodeId$elementId"
        return CodeImport(
            path = path,
            element = elementName
        )
    }

    open fun ReactFileBuilder.buildFileHeader(pageContext: ContentPage) {
        val name = pageContext.dri.first().sureClassNames
        addImport(CodeImport(path = "@storybook/addon-docs/blocks", element = "Meta", isComposite = true))
        write {
            append("<Meta title=\"$name\" parameters={{ previewTabs: { canvas: { hidden: true } } }} />")
            append("\n\n")
        }
    }
}