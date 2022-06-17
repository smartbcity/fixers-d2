package d2.dokka.storybook.translator.description

import d2.dokka.storybook.model.doc.DocumentableIndexes
import d2.dokka.storybook.model.doc.PageDocumentable
import d2.dokka.storybook.model.doc.RootDocumentable
import d2.dokka.storybook.model.doc.SectionDocumentable
import d2.dokka.storybook.model.render.D2TextStyle
import d2.dokka.storybook.model.doc.utils.documentableIn
import d2.dokka.storybook.model.doc.utils.toTypeString
import d2.dokka.storybook.translator.block
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.TextStyle

internal abstract class ModelDescriptionPageContentBuilder(
    private val contentBuilder: PageContentBuilder,
    override val documentableIndexes: DocumentableIndexes
): DescriptionPageContentBuilder() {

    override fun contentFor(d: Documentable): ContentNode? {
        return when (d) {
            is RootDocumentable -> contentFor(d)
            is PageDocumentable -> contentFor(d)
            is SectionDocumentable -> contentFor(d)
            is DClasslike -> contentFor(d)
            is DTypeAlias -> contentFor(d)
            else -> null
        }
    }

    private fun contentFor(c: DClasslike): ContentNode {
        return contentBuilder.contentFor(c)  {
            group(kind = ContentKind.Cover) {
                buildTitle(c)
                +contentForDescription(c)
            }

            group(styles = setOf(ContentStyle.TabbedContent)) {
                +contentForComments(c)
                propertiesBlock(c.properties)
            }
        }
    }

    private fun contentFor(t: DTypeAlias): ContentNode {
        return contentBuilder.contentFor(t)  {
            group(kind = ContentKind.Cover) {
                buildTitle(t)
                +contentForDescription(t)
            }
        }
    }

    private fun contentFor(r: RootDocumentable): ContentNode {
        return contentBuilder.contentFor(r)  {
            group(kind = ContentKind.Cover) {
                buildTitle(r)
                comment(r.pageDocumentation!!.description!!.root)
            }
        }
    }

    private fun contentFor(p: PageDocumentable): ContentNode {
        return contentBuilder.contentFor(p)  {
            group(kind = ContentKind.Cover) {
                buildTitle(p)
                +contentForDescription(p)
            }
        }
    }

    private fun contentFor(s: SectionDocumentable): ContentNode {
        return contentBuilder.contentFor(s)  {
            group(kind = ContentKind.Cover) {
                buildTitle(s)
                +contentForDescription(s)
            }
        }
    }

    private fun PageContentBuilder.DocumentableContentBuilder.propertiesBlock(
        properties: Collection<DProperty>,
    ) {
        block(kind = ContentKind.Properties, elements = properties) { property ->
            text(property.name, styles = setOf(TextStyle.Italic, TextStyle.Bold))

            val propertyTypeDocumentable = property.type.documentableIn(documentableIndexes.documentables)
            if (propertyTypeDocumentable == null) {
                text(property.type.toTypeString(), styles = setOf(D2TextStyle.Code))
            } else {
                link(text = property.type.toTypeString(), address = propertyTypeDocumentable.dri, styles = setOf(D2TextStyle.Code))
            }

            group(setOf(property.dri), property.sourceSets.toSet(), ContentKind.Main) {
                property.documentation.forEach { (_, docNode) ->
                    docNode.children.firstOrNull()?.root?.let {
                        group(kind = ContentKind.Comment) {
                            comment(it)
                        }
                    }
                }
            }
        }
    }
}
