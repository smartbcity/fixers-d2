package d2.dokka.storybook.model.doc.utils

import d2.dokka.storybook.model.doc.D2DocTagExtra
import d2.dokka.storybook.model.doc.PageDocumentable
import d2.dokka.storybook.model.doc.RootDocumentable
import d2.dokka.storybook.model.doc.SectionDocumentable
import d2.dokka.storybook.model.doc.tag.D2
import d2.dokka.storybook.model.doc.tag.D2DocTagWrapper
import d2.dokka.storybook.model.doc.tag.D2Type
import d2.dokka.storybook.model.doc.tag.Order
import d2.dokka.storybook.model.doc.tag.Title
import d2.dokka.storybook.model.doc.tag.Visual
import d2.dokka.storybook.model.doc.tag.VisualType
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.links.sureClassNames
import org.jetbrains.dokka.model.Annotations
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DEnum
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.properties.PropertyContainer
import org.jetbrains.dokka.model.properties.WithExtraProperties

private const val ROOT_SUFFIX = "Root"

fun Documentable.toRootDocumentable() = RootDocumentable(
	name = "${name.orEmpty()}$ROOT_SUFFIX",
	dri = dri.copy(classNames = "${dri.sureClassNames}$ROOT_SUFFIX"),
	documentation = documentation,
	sourceSets = sourceSets,
	expectPresentInSet = expectPresentInSet,
	children = listOf(this),
	extra = (this as? WithExtraProperties<Documentable>)?.extra ?: PropertyContainer.empty()
)

fun Documentable.toPageDocumentable() = PageDocumentable(
	name = name.orEmpty(),
	dri = dri.copy(classNames = dri.sureClassNames),
	documentation = documentation,
	sourceSets = sourceSets,
	expectPresentInSet = expectPresentInSet,
	children = listOf(this),
	extra = (this as? WithExtraProperties<Documentable>)?.extra ?: PropertyContainer.empty()
)

fun Documentable.toSectionDocumentable() = SectionDocumentable(
	name = name.orEmpty(),
	dri = dri.copy(classNames = dri.sureClassNames),
	documentation = documentation,
	sourceSets = sourceSets,
	expectPresentInSet = expectPresentInSet,
	children = listOf(this),
	extra = (this as? WithExtraProperties<Documentable>)?.extra ?: PropertyContainer.empty()
)

fun Documentable.d2Type() = d2DocTagExtra().firstTagOfTypeOrNull<D2>()?.type
fun Documentable.weight() = d2DocTagExtra().firstTagOfTypeOrNull<Order>()?.weight
fun Documentable.title(): String = if (this is RootDocumentable) {
	pageDocumentation?.title?.body ?: children.first().title()
} else {
	d2DocTagExtra().firstTagOfTypeOrNull<Title>()?.body
		?: generateTitle()
}.trim()

private fun Documentable.generateTitle() = when (d2Type()) {
	D2Type.COMMAND -> "Command"
	D2Type.QUERY -> "Query"
	D2Type.EVENT -> "Event"
	D2Type.RESULT -> "Result"
	D2Type.FUNCTION -> name!!.split(Regex("(?=[A-Z])"))
		.joinToString(" ")
		.substringBeforeLast("Function")
	else -> name!!
}

fun Documentable.asD2TypeDocumentable() = when (d2Type()) {
	D2Type.PAGE -> toPageDocumentable()
	D2Type.SECTION -> toSectionDocumentable()
	else -> this
}

inline fun <reified T : D2DocTagWrapper> Documentable.hasD2TagOfType(): Boolean {
	return d2DocTagExtra().firstTagOfTypeOrNull<T>() != null
}

fun Documentable.isOfType(vararg types: D2Type): Boolean {
	return d2DocTagExtra().firstTagOfTypeOrNull<D2>()?.type in types
}

fun Documentable.d2DocTagExtra() = (this as? WithExtraProperties<Documentable>)
	?.extra?.get(D2DocTagExtra)
	?: D2DocTagExtra(emptyList())

fun Documentable.visualType() = when (d2Type()) {
	D2Type.API -> VisualType.NONE
	D2Type.SERVICE -> VisualType.NONE
	null -> VisualType.NONE
	else -> modelVisualType()
}

private fun Documentable.modelVisualType() = if (this is RootDocumentable) {
	pageDocumentation?.visual?.type ?: VisualType.NONE
} else {
	d2DocTagExtra().firstTagOfTypeOrNull<Visual>()?.type ?: defaultVisualType()
}

private fun Documentable.defaultVisualType() = when (this) {
	is DEnum -> VisualType.NONE
	is DClasslike -> VisualType.DEFAULT
	else -> VisualType.NONE
}

fun Documentable.directAnnotation(dri: DRI): Annotations.Annotation? = (this as? WithExtraProperties<Documentable>)
	?.extra
	?.get(Annotations)
	?.directAnnotations
	.orEmpty()
	.flatMap { it.value }
	.firstOrNull { annotation -> annotation.dri == dri }
