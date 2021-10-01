package d2.dokka.storybook.renderer

import d2.dokka.storybook.builder.ReactFileBuilder
import d2.dokka.storybook.location.D2StorybookLocationProvider
import d2.dokka.storybook.model.code.BasicImportedElement
import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.imports.CodeImport
import d2.dokka.storybook.model.code.react.BasicComponent
import d2.dokka.storybook.model.code.react.CodeHighlighterComponent
import d2.dokka.storybook.model.code.react.DescriptedCodeComponent
import d2.dokka.storybook.model.doc.title
import d2.dokka.storybook.model.page.FileData
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.links.sureClassNames
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentText

open class MainPageContentRenderer: D2ContentRenderer {

    companion object {
        val RAW_LOADED_FILES = listOf(FileData.VISUAL_KOTLIN, FileData.VISUAL_YAML)
    }

    override lateinit var d2LocationProvider: D2StorybookLocationProvider

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
        append(element.toSourceComponent(node, pageContext))
    }

    open fun ReactFileBuilder.buildTwoColumnsSources(node: ContentGroup, pageContext: ContentPage) {
        val (leftElement, rightElement) = node.children as List<ContentText>

        val component = DescriptedCodeComponent(
            leftElement = leftElement.toSourceComponent(node, pageContext),
            rightElement = rightElement.toSourceComponent(node, pageContext)
        )
        append(component)
    }

    private fun ContentText.toSourceComponent(parent: ContentGroup, pageContext: ContentPage): CodeElement {
        val fileData = FileData.fromId(this.text)
        val codeImport = buildImport(parent.dci.dri.first(), fileData, parent.sourceSets, pageContext)

        return when (this.dci.kind) {
            ContentKind.Comment -> BasicComponent(importData = codeImport!!)
            ContentKind.Sample -> {
                val visual = BasicImportedElement(importData = codeImport!!)
                CodeHighlighterComponent(displayed = visual, language = fileData.language.id, title = "Example")
            }
            else -> throw IllegalArgumentException("Unsupported ContentKind[${this.dci.kind}] for source files")
        }
    }

    open fun ReactFileBuilder.importExtensions(node: ContentGroup, pageContext: ContentPage) {
        node.dci.dri.forEach { childDri ->
            val import = buildImport(childDri, FileData.MAIN, node.sourceSets, pageContext)
                ?: return@forEach
            val component = BasicComponent(
                identifier = import.element,
                importData = import
            )
            appendNewLine()
            appendNewLine()
            append(component)
        }
    }


    open fun buildImport(target: DRI, fileData: FileData, sourceSets: Set<DisplaySourceSet>, pageContext: ContentPage): CodeImport? {
        val fullDri = target.copy(extra = fileData.id)
        return d2LocationProvider.resolve(fullDri, sourceSets, pageContext)
            ?.let { path ->
                buildImport(fullDri, fileData, path)
            }
    }

    open fun buildImport(target: DRI, fileData: FileData, path: String): CodeImport {
        val nodeId = target.sureClassNames.capitalize()
        val elementId = fileData.id.capitalize()
        val elementName = "$nodeId$elementId"
        return CodeImport(
            path = path,
            element = elementName,
            withRawLoader = fileData in RAW_LOADED_FILES
        )
    }

    open fun ReactFileBuilder.buildFileHeader(pageContext: ContentPage) {
        val name = pageContext.documentable!!.title
        addImport(CodeImport(path = "@storybook/addon-docs/blocks", element = "Meta", isComposite = true))
        write {
            append("<Meta title=\"$name\" parameters={{ previewTabs: { canvas: { hidden: true } } }} />")
            append("\n\n")
        }
    }
}