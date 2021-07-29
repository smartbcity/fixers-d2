package d2.dokka.storybook.translator

import d2.dokka.storybook.model.doc.Example
import d2.dokka.storybook.model.doc.ExampleLink
import d2.dokka.storybook.model.doc.ExampleText
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
            is DProperty -> contentFor(d)
            else -> null
        }
    }

    private fun contentFor(c: DClasslike): ContentNode {
        return contentBuilder.contentFor(c, kind = ContentKind.Properties)  {
            +c.properties.mapNotNull(this@SamplePageContentBuilder::contentFor)
        }
    }

    private fun contentFor(r: RootDocumentable): ContentNode {
        return contentBuilder.contentFor(r, kind = ContentKind.Sample) {
            val exampleTag = r.pageDocumentation!!.example!!
            when (exampleTag) {
                is ExampleText -> codeBlock(exampleTag.body ?: "{}", "json")
                is ExampleLink -> contentForLinkedExample(r, exampleTag)?.let { +it }
            }
        }
    }

    private fun contentFor(property: DProperty): ContentNode? {
        return property.documentation.firstD2TagOfTypeOrNull<Example>()
            ?.let { exampleTag -> contentForTaggedProperty(property, exampleTag) }
            ?: contentForUntaggedProperty(property)
    }

    private fun contentForTaggedProperty(property: DProperty, exampleTag: Example): ContentNode? {
        return when (exampleTag) {
            is ExampleLink -> contentForLinkedExample(property, exampleTag)
            is ExampleText -> exampleTag.body?.let { body ->
                contentFor(property) { text(body) }
            }
        }
    }

    private fun contentForLinkedExample(d: Documentable, exampleTag: ExampleLink): ContentNode? {
        val targetDri = exampleTag.target ?: return null

        if (targetDri.callable == null) {
            return documentables[targetDri]?.let(this::contentFor)
        }

        val targetDocumentable = documentables[targetDri.copy(callable = null)]
        if (targetDocumentable !is DClasslike) {
            return null
        }
        val targetProperty = targetDocumentable.properties.find { it.name == targetDri.callable!!.name } ?: return null
        return contentFor(targetProperty.copy(name = d.name!!))
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

        return contentFor(property, styles = styles) { +contentForPropertyType }
    }

    private fun contentFor(property: DProperty, kind: ContentKind = ContentKind.Main, styles: Set<Style> = emptySet(), propertyValue: PageContentBuilder.DocumentableContentBuilder.() -> Unit): ContentGroup {
        return contentBuilder.contentFor(property, sourceSets = property.sourceSets, kind = kind, styles = styles) {
            text(property.name)
            propertyValue()
        }
    }
}