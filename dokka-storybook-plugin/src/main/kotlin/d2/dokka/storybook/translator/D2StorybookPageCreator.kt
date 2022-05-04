package d2.dokka.storybook.translator

import com.intellij.util.containers.BidirectionalMap
import d2.dokka.storybook.model.doc.PageDocumentable
import d2.dokka.storybook.model.doc.RootDocumentable
import d2.dokka.storybook.model.doc.SectionDocumentable
import d2.dokka.storybook.model.doc.asD2TypeDocumentable
import d2.dokka.storybook.model.doc.d2DocTagExtra
import d2.dokka.storybook.model.doc.tag.Child
import d2.dokka.storybook.model.doc.tag.Parent
import d2.dokka.storybook.model.doc.toRootDocumentable
import d2.dokka.storybook.model.doc.visualType
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

    private lateinit var documentablesMap: MutableMap<DRI, Documentable>
    private val childToParentBiMap = BidirectionalMap<DRI, DRI>()

    override fun pageForModule(m: DModule): ModulePageNode {
        val documentables = m.packages.flatMap { pack -> pack.classlikes + pack.typealiases }
            .map(Documentable::asD2TypeDocumentable)

        documentablesMap = documentables.associateBy(Documentable::dri).toMutableMap()
        buildChildrenMap(documentables)

        val pages = childToParentBiMap.getKeysByValue(DRI.topLevel)
            .orEmpty()
            .mapNotNull(documentablesMap::get)
            .plus(documentables)
            .flatMap(::pagesFor)

        return ModulePageNode(
            name = m.name.ifEmpty { "<root>" },
            content = contentForModule(m),
            documentables = listOf(m),
            children = pages
        )
    }

    private fun buildChildrenMap(documentables: List<Documentable>) {
        val parentMap = documentables.flatMap { documentable ->
            val parentDri = documentable.d2DocTagExtra()
                .firstTagOfTypeOrNull<Parent>()
                ?.target

            val childrenDri = documentable.d2DocTagExtra()
                .filterTagsOfType<Child>()
                .mapNotNull(Child::target)

            if (parentDri != null) {
                listOf(documentable.dri to parentDri)
            } else {
                emptyList()
            }.plus(childrenDri.map { it to documentable.dri })
        }.toMap().toMutableMap()

        documentables
            .filter { documentable -> documentable.dri !in parentMap }
            .filter { documentable -> documentable !is PageDocumentable }
            .forEach { documentable ->
                val rootDocumentable = documentable.toRootDocumentable()
                documentablesMap[rootDocumentable.dri] = rootDocumentable
                parentMap.putAll(listOf(
                    documentable.dri to rootDocumentable.dri,
                    rootDocumentable.dri to DRI.topLevel
                ))
            }
        childToParentBiMap.putAll(parentMap)
    }

    private fun pagesFor(d: Documentable): List<ModelPageNode> {
        val visualFileData = d.visualType()?.fileData
        val pagesToGenerate = when (d) {
            is RootDocumentable -> listOfNotNull(
                FileData.ROOT,
                FileData.MAIN,
                FileData.DESCRIPTION.takeIf { d.hasDescription },
                visualFileData
            )
            is PageDocumentable -> listOfNotNull(
                FileData.ROOT,
                FileData.MAIN,
                FileData.DESCRIPTION.takeIf { d.hasDescription },
                visualFileData
            )
            is SectionDocumentable -> listOfNotNull(
                FileData.MAIN,
                FileData.DESCRIPTION.takeIf { d.hasDescription },
                visualFileData
            )
            is DClasslike -> listOfNotNull(
                FileData.MAIN,
                FileData.DESCRIPTION,
                visualFileData
            )
            is DTypeAlias -> listOfNotNull(
                FileData.MAIN,
                FileData.DESCRIPTION,
                visualFileData
            )
            else -> emptyList()
        }

        return d.toPageNodes(pagesToGenerate)
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
        FileData.VISUAL_JSON,
        FileData.VISUAL_KOTLIN,
        FileData.VISUAL_YAML -> InnerVisualPageContentBuilder()
    }

    private inner class InnerMainPageContentBuilder:
        MainPageContentBuilder(contentBuilder, documentablesMap, childToParentBiMap)
    private inner class InnerDescriptionPageContentBuilder:
        DescriptionPageContentBuilder(contentBuilder, documentablesMap, childToParentBiMap) {
        override fun contentForComments(
            d: Documentable
        ): List<ContentNode> = this@D2StorybookPageCreator.contentForComments(d)
        override fun contentForDescription(
            d: Documentable
        ): List<ContentNode> = this@D2StorybookPageCreator.contentForDescription(d)
    }
    private inner class InnerVisualPageContentBuilder: VisualPageContentBuilder(contentBuilder, documentablesMap)
    private inner class InnerRootPageContentBuilder: RootPageContentBuilder(contentBuilder)
}
