package d2.dokka.storybook.translator

import d2.dokka.storybook.model.doc.Example
import d2.dokka.storybook.model.doc.Parent
import d2.dokka.storybook.model.doc.docTagWrappers
import d2.dokka.storybook.model.doc.firstD2TagOfTypeOrNull
import d2.dokka.storybook.model.page.FileData
import d2.dokka.storybook.model.page.ModelPageNode
import d2.dokka.storybook.model.render.D2TextStyle
import d2.dokka.storybook.model.render.toTypeString
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.base.signatures.SignatureProvider
import org.jetbrains.dokka.base.transformers.pages.comments.CommentsToContentConverter
import org.jetbrains.dokka.base.translators.documentables.DefaultPageCreator
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder.DocumentableContentBuilder
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DInterface
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.WithScope
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.TextStyle
import org.jetbrains.dokka.utilities.DokkaLogger

class D2StorybookPageCreator(
    configuration: DokkaBaseConfiguration?,
    commentsToContentConverter: CommentsToContentConverter,
    signatureProvider: SignatureProvider,
    logger: DokkaLogger
): DefaultPageCreator(configuration, commentsToContentConverter, signatureProvider, logger) {

    private lateinit var childrenMap: Map<DRI, List<DRI>>
    private val pageIndex = HashMap<DRI, ModelPageNode>()

    override fun pageForModule(m: DModule): ModulePageNode {
        val documentables = m.packages.flatMap { pack -> pack.classlikes + pack.typealiases }
        buildChildrenMap(documentables)

        return ModulePageNode(
            name = m.name.ifEmpty { "<root>" },
            content = contentForModule(m),
            documentable = m,
            children = documentables.flatMap(::pagesForDocumentable).withChildren()
        )
    }

    private fun buildChildrenMap(documentables: List<Documentable>) {
        childrenMap = documentables.mapNotNull { classlike ->
            classlike.documentation.firstD2TagOfTypeOrNull<Parent>()
                ?.target
                ?.let { it to classlike.dri }
        }.groupBy(Pair<DRI, DRI>::first, Pair<DRI, DRI>::second)
    }

    private fun pagesForDocumentable(documentable: Documentable): List<ModelPageNode> {
        val pages = when (documentable) {
            is DClasslike -> pagesForClasslike(documentable)
            is DTypeAlias -> pagesForTypeAlias(documentable)
            else -> emptyList()
        }

        pages.forEach { page -> pageIndex[page.dri.first()] = page }

        return pages
    }

    private fun pagesForTypeAlias(t: DTypeAlias): List<ModelPageNode> {
        return listOf(
            t.toMainPage(),
            t.toDescriptionPage()
        )
    }

    private fun pagesForClasslike(c: DClasslike): List<ModelPageNode> {
        return listOf(
            c.toMainPage(),
            c.toDescriptionPage(),
            c.toSamplePage()
        )
    }

    private fun List<ModelPageNode>.withChildren(): List<ModelPageNode> {
        return map { page ->
            when (page.fileData) {
                FileData.MAIN -> page.withChildren()
                else -> page
            }
        }
    }

    private fun ModelPageNode.withChildren(): ModelPageNode {
        val mainDri = dri.first().copy(extra = null)

        val children = childrenMap[mainDri].orEmpty()
            .mapNotNull { childDri -> pageIndex[childDri.copy(extra = FileData.MAIN.id)] }

        return modified(children = children)
    }

    private fun DClasslike.toMainPage(): ModelPageNode {
        return toModelPageNode(
            content = mainContentForClasslike(this),
            fileData = FileData.MAIN
        )
    }

    private fun mainContentForClasslike(c: DClasslike): ContentGroup {
        if (c is DInterface) {
            return contentBuilder.contentFor(c, kind = ContentKind.Main)  {
                group(setOf(c.dri), kind = ContentKind.Source) {
                    text(FileData.DESCRIPTION.id, kind = ContentKind.Comment)
                    text(FileData.SAMPLE.id, kind = ContentKind.Sample)
                }
                group(childrenMap[c.dri]?.toSet() ?: emptySet(), kind = ContentKind.Extensions) {}
            }
        }

        return contentBuilder.contentFor(c, kind = ContentKind.Empty)
    }

    private fun DTypeAlias.toMainPage(): ModelPageNode {
        return toModelPageNode(
            content = mainContentForTypeAlias(this),
            fileData = FileData.MAIN
        )
    }

    private fun mainContentForTypeAlias(t: DTypeAlias): ContentGroup {
        return contentBuilder.contentFor(t, kind = ContentKind.Main)  {
            group(setOf(t.dri), kind = ContentKind.Source) {
                text(FileData.DESCRIPTION.id, kind = ContentKind.Comment)
            }
            group(childrenMap[t.dri]?.toSet() ?: emptySet(), kind = ContentKind.Extensions) {}
        }
    }

    private fun DClasslike.toDescriptionPage(): ModelPageNode {
        return toModelPageNode(
            content = descriptionContentForClasslike(this),
            fileData = FileData.DESCRIPTION
        )
    }

    private fun descriptionContentForClasslike(c: DClasslike): ContentGroup {
        if (c is DInterface) {
            return contentBuilder.contentFor(c)  {
                group(kind = ContentKind.Cover) {
                    header(2, c.name)
                    +contentForDescription(c)
                }

                group(styles = setOf(ContentStyle.TabbedContent)) {
                    +contentForComments(c)
                    +contentForScope(c, c.dri, c.sourceSets)
                }
            }
        }

        return super.contentForClasslike(c)
    }

    private fun DTypeAlias.toDescriptionPage(): ModelPageNode {
        return toModelPageNode(
            content = descriptionContentForTypeAlias(this),
            fileData = FileData.DESCRIPTION
        )
    }

    private fun descriptionContentForTypeAlias(t: DTypeAlias): ContentGroup {
        return contentBuilder.contentFor(t)  {
            group(kind = ContentKind.Cover) {
                header(2, t.name)
                +contentForDescription(t)
            }
        }
    }

    override fun contentForScope(
        s: WithScope, dri: DRI, sourceSets: Set<DokkaConfiguration.DokkaSourceSet>
    ): ContentGroup {
        if (s is DInterface) {
            return contentBuilder.contentFor(s as Documentable)  {
                propertiesBlock(s.properties)
            }
        }

        return super.contentForScope(s, dri, sourceSets)
    }

    private fun DocumentableContentBuilder.propertiesBlock(
        properties: Collection<DProperty>,
    ) {
        block(kind = ContentKind.Properties, elements = properties) { property ->
            text(property.name, styles = setOf(TextStyle.Italic))
            text(property.type.toTypeString(), styles = setOf(D2TextStyle.Code))
            group(setOf(property.dri), property.sourceSets.toSet(), ContentKind.Main) {
                property.sourceSets.forEach { sourceSet ->
                    property.documentation[sourceSet]?.children?.firstOrNull()?.root?.let {
                        group(kind = ContentKind.Comment) {
                            comment(it)
                        }
                    }
                }
            }
        }
    }

    private fun DClasslike.toSamplePage(): ModelPageNode {
        return toModelPageNode(
            content = sampleContentForClasslike(this),
            fileData = FileData.SAMPLE
        )
    }

    private fun sampleContentForClasslike(c: DClasslike): ContentGroup {
        if (c is DInterface) {
            return contentBuilder.contentFor(c, kind = ContentKind.Properties)  {
                +contentForExamples(c)
            }
        }

        return contentBuilder.contentFor(c, kind = ContentKind.Empty)
    }

    private fun contentForExamples(c: DClasslike): List<ContentGroup> {
        return c.properties.mapNotNull { property ->
            val (_, d2TagWrappers) = property.documentation.docTagWrappers()
            val exampleTag = (d2TagWrappers.firstOrNull { it is Example } as Example?)

            exampleTag?.body?.let { exampleTagBody ->
                contentBuilder.contentFor(property, sourceSets = property.sourceSets, kind = ContentKind.Main) {
                    text(property.name)
                    text(exampleTagBody)
                }
            }
        }
    }

    private fun Documentable.toModelPageNode(content: ContentNode, fileData: FileData): ModelPageNode {
        return ModelPageNode(
            name = this.name.orEmpty(),
            content = content,
            dri = setOf(this.dri.copy(extra = fileData.id)),
            documentable = this,
            children = emptyList(),
            fileData = fileData
        )
    }
}
