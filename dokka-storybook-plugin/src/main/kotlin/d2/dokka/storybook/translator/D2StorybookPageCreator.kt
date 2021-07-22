package d2.dokka.storybook.translator

import d2.dokka.storybook.model.doc.Parent
import d2.dokka.storybook.model.doc.RootDocumentable
import d2.dokka.storybook.model.doc.firstD2TagOfTypeOrNull
import d2.dokka.storybook.model.doc.toRootDocumentable
import d2.dokka.storybook.model.page.FileData
import d2.dokka.storybook.model.page.ModelPageNode
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.base.signatures.SignatureProvider
import org.jetbrains.dokka.base.transformers.pages.comments.CommentsToContentConverter
import org.jetbrains.dokka.base.translators.documentables.DefaultPageCreator
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.utilities.DokkaLogger

class D2StorybookPageCreator(
    configuration: DokkaBaseConfiguration?,
    commentsToContentConverter: CommentsToContentConverter,
    signatureProvider: SignatureProvider,
    logger: DokkaLogger
): DefaultPageCreator(configuration, commentsToContentConverter, signatureProvider, logger) {

    private lateinit var childrenMap: Map<DRI, List<Documentable>>
    private val pageIndex = HashMap<DRI, ModelPageNode>()

    override fun pageForModule(m: DModule): ModulePageNode {
        val documentables = m.packages.flatMap { pack -> pack.classlikes + pack.typealiases }
        buildChildrenMap(documentables)

        val rootPages = childrenMap[DRI.topLevel]
            ?.flatMap(::pagesFor)
            ?: emptyList()

        return ModulePageNode(
            name = m.name.ifEmpty { "<root>" },
            content = contentForModule(m),
            documentable = m,
            children = documentables.flatMap(this::pagesFor).plus(rootPages)
        )
    }

    private fun buildChildrenMap(documentables: List<Documentable>) {
        childrenMap = documentables.flatMap { documentable ->
            val parentDri = documentable.documentation
                .firstD2TagOfTypeOrNull<Parent>()
                ?.target

            if (parentDri != null) {
                listOf(parentDri to documentable)
            } else {
                val rootDocumentable = documentable.toRootDocumentable()
                listOf(
                    DRI.topLevel to rootDocumentable,
                    rootDocumentable.dri to documentable
                )
            }
        }.groupBy(Pair<DRI, Documentable>::first, Pair<DRI, Documentable>::second)
    }

    private fun pagesFor(documentable: Documentable): List<ModelPageNode> {
        val pagesToGenerate = when (documentable) {
            is DClasslike -> listOf(FileData.MAIN, FileData.DESCRIPTION, FileData.SAMPLE)
            is DTypeAlias -> listOf(FileData.MAIN, FileData.DESCRIPTION)
            is RootDocumentable -> pagesFor(documentable)
            else -> emptyList()
        }

        val pages = documentable.toPageNodes(pagesToGenerate)
        pages.forEach { page -> pageIndex[page.dri.first()] = page }
        return pages
    }

    private fun pagesFor(r: RootDocumentable): List<FileData> {
        return listOfNotNull(
            FileData.ROOT,
            FileData.MAIN.takeIf { r.hasDescription || r.hasExample },
            FileData.DESCRIPTION.takeIf { r.hasDescription },
            FileData.SAMPLE.takeIf { r.hasExample }
        )
    }

    private fun Documentable.toPageNodes(files: List<FileData>): List<ModelPageNode> {
        return files.mapNotNull { toModelPageNode(it) }
    }

    private fun Documentable.toModelPageNode(fileData: FileData): ModelPageNode? {
        return fileData.contentBuilder().contentFor(this)
            ?.let { content ->
                ModelPageNode(
                    name = this.name.orEmpty(),
                    content = content,
                    dri = setOf(this.dri.copy(extra = fileData.id)),
                    documentable = this,
                    children = emptyList(),
                    fileData = fileData
                )
            }
    }

    private fun FileData.contentBuilder() = when (this) {
        FileData.ROOT -> InnerRootPageContentBuilder()
        FileData.MAIN -> InnerMainPageContentBuilder()
        FileData.DESCRIPTION -> InnerDescriptionPageContentBuilder()
        FileData.SAMPLE -> InnerSamplePageContentBuilder()
    }

    private inner class InnerMainPageContentBuilder: MainPageContentBuilder(contentBuilder, childrenMap)
    private inner class InnerDescriptionPageContentBuilder: DescriptionPageContentBuilder(contentBuilder) {
        override fun contentForComments(d: Documentable): List<ContentNode> = this@D2StorybookPageCreator.contentForComments(d)
        override fun contentForDescription(d: Documentable): List<ContentNode> = this@D2StorybookPageCreator.contentForDescription(d)
    }
    private inner class InnerSamplePageContentBuilder: SamplePageContentBuilder(contentBuilder)
    private inner class InnerRootPageContentBuilder: RootPageContentBuilder(contentBuilder)
}
