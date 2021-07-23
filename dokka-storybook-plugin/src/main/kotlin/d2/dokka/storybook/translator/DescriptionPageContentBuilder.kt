package d2.dokka.storybook.translator

import d2.dokka.storybook.model.doc.RootDocumentable
import d2.dokka.storybook.model.doc.Title
import d2.dokka.storybook.model.doc.firstD2TagOfTypeOrNull
import d2.dokka.storybook.model.render.D2TextStyle
import d2.dokka.storybook.model.render.toTypeString
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DInterface
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.TextStyle

internal abstract class DescriptionPageContentBuilder(
    protected val contentBuilder: PageContentBuilder
): D2StorybookPageContentBuilder {

    protected abstract fun contentForComments(d: Documentable): List<ContentNode>
    protected abstract fun contentForDescription(d: Documentable): List<ContentNode>

    override fun contentFor(d: Documentable): ContentNode? {
        return when (d) {
            is DClasslike -> contentFor(d)
            is DTypeAlias -> contentFor(d)
            is RootDocumentable -> contentFor(d)
            else -> null
        }
    }

    private fun contentFor(c: DClasslike): ContentNode? {
        if (c !is DInterface) {
            return null
        }

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

    private fun PageContentBuilder.DocumentableContentBuilder.buildTitle(d: Documentable) {
        val title = when (d) {
            is RootDocumentable -> d.pageDocumentation?.title?.body
            else -> d.documentation.firstD2TagOfTypeOrNull<Title>()?.body
        } ?: d.name!!

        header(2, title)
    }

    private fun PageContentBuilder.DocumentableContentBuilder.propertiesBlock(
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