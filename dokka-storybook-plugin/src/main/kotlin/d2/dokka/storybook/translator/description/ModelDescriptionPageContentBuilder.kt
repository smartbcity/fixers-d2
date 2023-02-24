package d2.dokka.storybook.translator.description

import d2.dokka.storybook.model.doc.DocumentableIndexes
import d2.dokka.storybook.model.doc.PageDocumentable
import d2.dokka.storybook.model.doc.RootDocumentable
import d2.dokka.storybook.model.doc.SectionDocumentable
import d2.dokka.storybook.model.doc.tag.D2Type
import d2.dokka.storybook.model.doc.tag.Ref
import d2.dokka.storybook.model.doc.utils.d2DocTagExtra
import d2.dokka.storybook.model.doc.utils.documentableIn
import d2.dokka.storybook.model.doc.utils.isOfType
import d2.dokka.storybook.model.doc.utils.title
import d2.dokka.storybook.model.doc.utils.toTypeString
import d2.dokka.storybook.model.render.D2TextStyle
import d2.dokka.storybook.translator.block
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DEnum
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.TypeAliased
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
            is DEnum -> contentFor(d)
            is DClasslike -> contentFor(d)
            is DTypeAlias -> contentFor(d)
            else -> null
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

    private fun contentFor(e: DEnum): ContentNode {
        return contentBuilder.contentFor(e)  {
            group(kind = ContentKind.Cover) {
                buildTitle(e)
                +contentForDescription(e)
            }

            unorderedList {
                e.entries.forEach { entry ->
                    item {
                        text(entry.name, styles = setOf(TextStyle.Bold))
                        text(": ")
                        entry.documentation.forEach { (_, docNode) ->
                            docNode.children.firstOrNull()?.root?.let {
                                firstSentenceComment(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun contentFor(c: DClasslike): ContentNode {
        return contentBuilder.contentFor(c)  {
            group(kind = ContentKind.Cover) {
                buildTitle(c)
                buildType(c)
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
                buildType(t)
                +contentForDescription(t)
            }
        }
    }

    private fun PageContentBuilder.DocumentableContentBuilder.propertiesBlock(
        properties: Collection<DProperty>,
    ) {
        block(kind = ContentKind.Properties, elements = properties) { property ->
            text(property.name, styles = setOf(TextStyle.Italic, TextStyle.Bold))
            buildProperty(property)
        }
    }

    private fun PageContentBuilder.DocumentableContentBuilder.buildProperty(p: DProperty) {
        val ref = p.d2DocTagExtra().firstTagOfTypeOrNull<Ref>()?.target
        if (ref != null) {
            if (ref.callable == null) {
                throw IllegalArgumentException("Tag @ref of a property must link to a property (${p.dri} -> $ref")
            }
            val refProperty = (documentableIndexes.documentables[ref.copy(callable = null)] as DClasslike)
                .properties
                .first { it.name == ref.callable!!.name }

            return buildProperty(refProperty)
        }

        val propertyType = p.type
        val propertyTypeDocumentable = propertyType.documentableIn(documentableIndexes.documentables)
        if (propertyTypeDocumentable == null || propertyTypeDocumentable.isOfType(D2Type.HIDDEN)) {
            val typeString = if (propertyType is TypeAliased) {
                propertyType.inner.toTypeString(documentableIndexes.documentables)
            } else {
                propertyType.toTypeString(documentableIndexes.documentables)
            }
            text(typeString, styles = setOf(D2TextStyle.Code))
        } else {
            link(
                text = propertyType.toTypeString(documentableIndexes.documentables),
                address = propertyTypeDocumentable.dri,
                styles = setOf(D2TextStyle.Code)
            )
        }

        group(setOf(p.dri), p.sourceSets.toSet(), ContentKind.Main) {
            p.documentation.forEach { (_, docNode) ->
                docNode.children.firstOrNull()?.root?.let {
                    group(kind = ContentKind.Comment) {
                        comment(it)
                    }
                }
            }
        }
    }

    private fun PageContentBuilder.DocumentableContentBuilder.buildType(d: Documentable) {
        if (d.title() != d.name) {
            text("Type: ")
            text(d.name!!, styles = setOf(D2TextStyle.Code))
        }
    }
}
