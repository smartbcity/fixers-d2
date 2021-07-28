package d2.dokka.storybook.translator

import d2.dokka.storybook.model.doc.Example
import d2.dokka.storybook.model.doc.RootDocumentable
import d2.dokka.storybook.model.doc.firstD2TagOfTypeOrNull
import d2.dokka.storybook.model.render.documentableIn
import d2.dokka.storybook.model.render.isCollection
import d2.dokka.storybook.model.render.isMap
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.Style

abstract class SamplePageContentBuilder(
    protected val contentBuilder: PageContentBuilder,
    protected val documentables: Map<DRI, Documentable>
): D2StorybookPageContentBuilder {

    override fun contentFor(d: Documentable): ContentNode? {
        return when (d) {
            is DClasslike -> contentFor(d)
            is RootDocumentable -> contentFor(d)
            else -> null
        }
    }

    private fun contentFor(c: DClasslike): ContentNode {
        return contentBuilder.contentFor(c, kind = ContentKind.Properties)  {
            +contentForExamples(c)
        }
    }

    private fun contentFor(r: RootDocumentable): ContentNode {
        return contentBuilder.contentFor(r, kind = ContentKind.Sample) {
            codeBlock(r.pageDocumentation!!.example!!.body, "json")
        }
    }

    private fun contentForExamples(c: DClasslike): List<ContentGroup> {
        return c.properties.mapNotNull { property ->
            property.documentation.firstD2TagOfTypeOrNull<Example>()
                ?.let { exampleTag -> contentForTaggedProperty(property, exampleTag) }
                ?: contentForUntaggedProperty(property)
        }
    }

    private fun contentForTaggedProperty(property: DProperty, exampleTag: Example): ContentGroup {
        return contentBuilder.contentFor(property, sourceSets = property.sourceSets, kind = ContentKind.Main) {
            text(property.name)
            text(exampleTag.body)
        }
    }

    private fun contentForUntaggedProperty(property: DProperty): ContentGroup? {
        val styles = setOfNotNull<Style>(
            ContentStyle.TabbedContent.takeIf { property.type.isCollection() && !property.type.isMap() }
        )

        val propertyType = property.type.documentableIn(documentables)
        if (propertyType == null || propertyType !is DClasslike) {
            return null
        }

        val contentForPropertyType = contentFor(propertyType)
        if (contentForPropertyType.children.isEmpty()) {
            return null
        }

        return contentBuilder.contentFor(property, sourceSets = property.sourceSets, kind = ContentKind.Main, styles = styles) {
            text(property.name)
            +contentForPropertyType
        }
    }

}