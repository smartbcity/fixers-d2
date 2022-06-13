package d2.dokka.storybook.model.doc

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
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.properties.PropertyContainer
import org.jetbrains.dokka.model.properties.WithExtraProperties

private const val ROOT_SUFFIX = "Root"

fun Documentable.toRootDocumentable() = RootDocumentable(
	name = "${name.orEmpty()}${ROOT_SUFFIX}",
	dri = dri.copy(classNames = "${dri.sureClassNames}${ROOT_SUFFIX}"),
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
	d2DocTagExtra().firstTagOfTypeOrNull<Title>()?.body ?: name!!
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
	return d2DocTagExtra().firstTagOfType<D2>().type in types
}

fun Documentable.d2DocTagExtra() = (this as? WithExtraProperties<Documentable>)
	?.extra?.get(D2DocTagExtra)
	?: D2DocTagExtra(emptyList())

fun Documentable.visualType() = when (d2Type()) {
	D2Type.SERVICE -> serviceVisualType()
	null -> null
	else -> modelVisualType()
}

private fun Documentable.modelVisualType() = when (this) {
	is RootDocumentable -> pageDocumentation?.visual?.type ?: VisualType.NONE
	is PageDocumentable -> d2DocTagExtra().firstTagOfTypeOrNull<Visual>()?.type ?: VisualType.NONE
	is SectionDocumentable -> d2DocTagExtra().firstTagOfTypeOrNull<Visual>()?.type ?: VisualType.NONE
	is DClasslike -> d2DocTagExtra().firstTagOfTypeOrNull<Visual>()?.type ?: VisualType.DEFAULT
	is DTypeAlias -> d2DocTagExtra().firstTagOfTypeOrNull<Visual>()?.type
	else -> null
}

private fun Documentable.serviceVisualType() = when (this) {
	else -> d2DocTagExtra().firstTagOfTypeOrNull<Visual>()?.type ?: VisualType.NONE
}

fun Documentable.directAnnotation(dri: DRI): Annotations.Annotation? = (this as? WithExtraProperties<Documentable>)
	?.extra
	?.get(Annotations)
	?.directAnnotations
	.orEmpty()
	.flatMap { it.value }
	.firstOrNull { annotation -> annotation.dri == dri }
