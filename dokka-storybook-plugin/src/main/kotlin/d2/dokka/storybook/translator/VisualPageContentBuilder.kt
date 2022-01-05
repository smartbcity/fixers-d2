package d2.dokka.storybook.translator

import d2.dokka.storybook.model.doc.PageDocumentable
import d2.dokka.storybook.model.doc.RootDocumentable
import d2.dokka.storybook.model.doc.SectionDocumentable
import d2.dokka.storybook.model.doc.d2DocTagExtra
import d2.dokka.storybook.model.doc.tag.Example
import d2.dokka.storybook.model.doc.tag.ExampleLink
import d2.dokka.storybook.model.doc.tag.ExampleText
import d2.dokka.storybook.model.doc.tag.Visual
import d2.dokka.storybook.model.doc.tag.VisualSimple
import d2.dokka.storybook.model.doc.tag.VisualText
import d2.dokka.storybook.model.doc.tag.VisualLink
import d2.dokka.storybook.model.doc.tag.WithTarget
import d2.dokka.storybook.model.render.documentableIn
import d2.dokka.storybook.model.render.isCollection
import d2.dokka.storybook.model.render.isMap
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.Style

abstract class VisualPageContentBuilder(
	protected val contentBuilder: PageContentBuilder,
	protected val documentables: Map<DRI, Documentable>
) : D2StorybookPageContentBuilder {

	override fun contentFor(d: Documentable): ContentNode? {
		return when (d) {
			is RootDocumentable -> rawContentFor(d)
			is PageDocumentable -> rawContentFor(d)
			is SectionDocumentable -> rawContentFor(d)
			is DClasslike -> contentFor(d)
			is DTypeAlias -> rawContentFor(d)
			is DProperty -> contentFor(d)
			else -> null
		}
	}

	private fun contentFor(c: DClasslike): ContentNode {
		val visualTag = c.d2DocTagExtra().firstTagOfTypeOrNull<Visual>()

		return if (visualTag == null || visualTag is VisualSimple) {
			contentBuilder.contentFor(c, kind = ContentKind.Properties) {
				header(0, c.name!!, kind = ContentKind.Symbol)
				+c.properties.mapNotNull(this@VisualPageContentBuilder::contentFor)
			}
		} else {
			rawContentForVisualTag(c, visualTag)
		}
	}

	private fun rawContentFor(r: RootDocumentable): ContentNode {
		val visualTag = r.pageDocumentation!!.visual!!
		return rawContentForVisualTag(r, visualTag)
	}

	private fun rawContentFor(d: Documentable): ContentNode? {
		return d.d2DocTagExtra().firstTagOfTypeOrNull<Visual>()?.let { rawContentForVisualTag(d, it) }
	}

	private fun rawContentForVisualTag(d: Documentable, visualTag: Visual): ContentNode {
		return contentBuilder.contentFor(d, kind = ContentKind.Sample) {
			when (visualTag) {
				is VisualSimple -> Unit
				is VisualText -> codeBlock(visualTag.body ?: "", "")
				is VisualLink -> contentForLinkedSample(d, visualTag)?.let { +it }
			}
		}
	}

	private fun contentFor(property: DProperty): ContentNode? {
		return property.d2DocTagExtra().firstTagOfTypeOrNull<Example>()
			?.let { exampleTag -> contentForTaggedProperty(property, exampleTag) }
			?: contentForUntaggedProperty(property)
	}

	private fun contentForTaggedProperty(property: DProperty, exampleTag: Example): ContentNode? {
		return when (exampleTag) {
			is ExampleLink -> contentForLinkedSample(property, exampleTag)
			is ExampleText -> exampleTag.body?.let { body ->
				contentFor(property) { text(body) }
			}
		}
	}

	private fun contentForLinkedSample(d: Documentable, targetTag: WithTarget): ContentNode? {
		val targetDri = targetTag.target ?: return null

		if (targetDri.callable == null) {
			return documentables[targetDri]?.let(::contentFor)
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
			?: return null

		val contentForPropertyType = contentFor(propertyType)
			?.takeIf { it.children.isNotEmpty() }
			?: return null

		return contentFor(property, styles = styles) { +contentForPropertyType }
	}

	private fun contentFor(
		property: DProperty, kind: ContentKind = ContentKind.Main, styles: Set<Style> = emptySet(),
		propertyValue: PageContentBuilder.DocumentableContentBuilder.() -> Unit
	): ContentGroup {
		return contentBuilder.contentFor(property, sourceSets = property.sourceSets, kind = kind, styles = styles) {
			text(property.name)
			propertyValue()
		}
	}
}
