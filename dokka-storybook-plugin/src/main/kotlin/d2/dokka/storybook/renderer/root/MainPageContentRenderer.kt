package d2.dokka.storybook.renderer.root

import d2.dokka.storybook.location.D2StorybookLocationProvider
import d2.dokka.storybook.model.code.BasicImportedElement
import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.imports.CodeImport
import d2.dokka.storybook.model.code.react.BasicComponent
import d2.dokka.storybook.model.code.react.g2.CodeHighlighterComponent
import d2.dokka.storybook.model.code.react.g2.SegmentedContainerComponent
import d2.dokka.storybook.model.code.react.storybook.MetaComponent
import d2.dokka.storybook.model.doc.utils.title
import d2.dokka.storybook.model.page.FileData
import d2.dokka.storybook.model.render.D2ContentKind
import d2.dokka.storybook.model.render.D2Marker
import d2.dokka.storybook.renderer.D2ContentRenderer
import d2.dokka.storybook.renderer.builder.ReactFileBuilder
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.links.sureClassNames
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentText

open class MainPageContentRenderer(
    private val isRoot: Boolean
): D2ContentRenderer {

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
            is ContentText -> buildMarker(node, pageContext)
            else -> throw IllegalArgumentException("Cannot render content of type [${node::class.java}] in a Main page")
        }
    }

    open fun ReactFileBuilder.buildMarker(node: ContentText, pageContext: ContentPage) {
        when (node.dci.kind) {
            D2Marker.Divider -> appendDivider()
            else -> throw IllegalArgumentException("Cannot render marker of type [${node.dci.kind}] in a Main page")
        }
    }

    open fun ReactFileBuilder.buildGroup(node: ContentGroup, pageContext: ContentPage) {
        when (node.dci.kind) {
            D2ContentKind.Container -> node.children.forEach { child -> buildContentNode(child, pageContext) }
            D2ContentKind.Source -> importSources(node, pageContext)
            D2ContentKind.Children -> importExtensions(node, pageContext)
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
        val element = node.children.first() as ContentGroup
        append(element.toSourceComponent(node, pageContext))
    }

    open fun ReactFileBuilder.buildTwoColumnsSources(node: ContentGroup, pageContext: ContentPage) {
        val (leftElement, rightElement) = node.children as List<ContentGroup>

        val leftContainerProps = mutableMapOf<String, String>()
        val rightContainerProps = mutableMapOf<String, String>()

        if (leftElement.dci.kind == D2ContentKind.Description && rightElement.dci.kind == D2ContentKind.Description) {
            leftContainerProps["width"] = "50%"
            rightContainerProps["width"] = "50%"
            rightContainerProps["position"] = "relative"
            rightContainerProps["paddingTop"] = "0"
        }

        val component = SegmentedContainerComponent(
            leftElement = leftElement.toSourceComponent(node, pageContext),
            leftContainerProps = leftContainerProps,
            rightElement = rightElement.toSourceComponent(node, pageContext),
            rightContainerProps = rightContainerProps
        )
        append(component)
    }

    private fun ContentGroup.toSourceComponent(parent: ContentGroup, pageContext: ContentPage): CodeElement {
        val title = children.firstOrNull { it.dci.kind == D2ContentKind.Description } as ContentText?
        val fileId = children.first { it.dci.kind == D2ContentKind.File } as ContentText

        val fileData = FileData.fromId(fileId.text)
        val codeImport = buildImport(parent.dci.dri.first(), fileData, parent.sourceSets, pageContext)

        return when (this.dci.kind) {
            D2ContentKind.Description -> BasicComponent(importData = codeImport!!)
            D2ContentKind.Visual -> {
                val visual = BasicImportedElement(importData = codeImport!!)
                CodeHighlighterComponent(displayed = visual, language = fileData.language.id, title = title?.text ?: "Example")
            }
            else -> throw IllegalArgumentException("Unsupported ContentKind[${this.dci.kind}] for source files")
        }
    }

    open fun ReactFileBuilder.importExtensions(node: ContentGroup, pageContext: ContentPage) {
        node.dci.dri.forEach { childDri ->
            val import = buildImport(childDri, FileData.MAIN, node.sourceSets, pageContext)
                ?: return@forEach

            appendNewLine()
            appendNewLine()
            append(BasicComponent(importData = import))
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
        if (!isRoot) return

        val name = pageContext.documentable!!.title()
        append(MetaComponent(name))
    }

    open fun ReactFileBuilder.appendDivider() {
        appendNewLine()
        appendNewLine()
        append("---")
    }
}
