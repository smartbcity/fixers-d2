package d2.dokka.storybook.translators

import d2.dokka.storybook.model.D2TextStyle
import d2.dokka.storybook.model.page.FileData
import d2.dokka.storybook.model.page.ModelPageNode
import d2.dokka.storybook.model.toTypeString
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
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.WithScope
import org.jetbrains.dokka.model.doc.CustomTagWrapper
import org.jetbrains.dokka.model.doc.TagWrapper
import org.jetbrains.dokka.model.doc.Text
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

    override fun pageForModule(m: DModule) =
        ModulePageNode(m.name.ifEmpty { "<root>" }, contentForModule(m), m, m.packages.flatMap(DPackage::classlikes).flatMap(::pagesForClasslike))

    private fun pagesForClasslike(c: DClasslike): List<ModelPageNode> {
        return listOf(
//            c.toMainPage(),
            c.toDescriptionPage(),
            c.toSamplePage()
        )
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

        return super.contentForClasslike(c)
    }

    private fun contentForExamples(c: DClasslike): List<ContentGroup> {
        return c.properties.map { property ->
            contentBuilder.contentFor(property, sourceSets = property.sourceSets, kind = ContentKind.Main) {
                property.sourceSets.forEach { sourceSet ->
                    property.documentation[sourceSet]?.children?.exampleTagValue()?.let { tagValue ->
                        text(property.name)
                        text(tagValue)
                    }
                }
            }
        }
    }

    private fun List<TagWrapper>.exampleTagValue(): String? {
        val exampleTag = firstOrNull { tag -> tag is CustomTagWrapper && tag.name == "example" }
            ?: return null

        // TODO better analyse
        val text = exampleTag.root
            .children
            .firstOrNull()
            ?.children
            ?.firstOrNull() as Text?

        return text?.body
    }

    private fun DClasslike.toModelPageNode(content: ContentNode, fileData: FileData): ModelPageNode {
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
