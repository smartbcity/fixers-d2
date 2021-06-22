package d2.dokka.storybook.translators

import d2.dokka.storybook.model.D2TextStyle
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
import org.jetbrains.dokka.pages.ClasslikePageNode
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
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
        ModulePageNode(m.name.ifEmpty { "<root>" }, contentForModule(m), m, m.packages.flatMap(DPackage::classlikes).map(::pageForClasslike))

    override fun pageForClasslike(c: DClasslike): ClasslikePageNode {
        return ClasslikePageNode(
            c.name.orEmpty(), contentForClasslike(c), setOf(), null, emptyList()
        )
    }

    override fun contentForClasslike(c: DClasslike): ContentGroup {
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
}
