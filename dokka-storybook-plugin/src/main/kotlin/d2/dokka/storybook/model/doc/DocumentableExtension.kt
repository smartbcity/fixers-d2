package d2.dokka.storybook.model.doc

import d2.dokka.storybook.model.doc.tag.D2
import d2.dokka.storybook.model.doc.tag.D2DocTagWrapper
import d2.dokka.storybook.model.doc.tag.D2Type
import d2.dokka.storybook.model.doc.tag.Order
import d2.dokka.storybook.model.doc.tag.Title
import org.jetbrains.dokka.links.sureClassNames
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.properties.PropertyContainer
import org.jetbrains.dokka.model.properties.WithExtraProperties

val Documentable.d2Type
    get() = d2DocTagExtra()
        .firstTagOfTypeOrNull<D2>()
        ?.type

val Documentable.weight
  get() = d2DocTagExtra()
      .firstTagOfTypeOrNull<Order>()
      ?.weight

fun Documentable.toRootDocumentable() = RootDocumentable(
    name = "${name.orEmpty()}Page",
    dri = dri.copy(classNames = "${dri.sureClassNames}Page"),
    documentation = documentation,
    sourceSets = sourceSets,
    expectPresentInSet = expectPresentInSet,
    children = listOf(this),
    extra = (this as? WithExtraProperties<Documentable>)?.extra ?: PropertyContainer.empty()
)

val Documentable.title
    get() = when (this) {
        is RootDocumentable -> pageDocumentation?.title?.body ?: name.removeSuffix("Page")
        else -> d2DocTagExtra().firstTagOfTypeOrNull<Title>()?.body ?: name!!
    }

inline fun <reified T: D2DocTagWrapper> Documentable.hasD2TagOfType(): Boolean {
    return d2DocTagExtra().firstTagOfTypeOrNull<T>() != null
}

fun Documentable.isOfType(type: D2Type): Boolean {
    return d2DocTagExtra().firstTagOfType<D2>().type == type
}

fun Documentable.d2DocTagExtra() = (this as? WithExtraProperties<Documentable>)
    ?.extra?.get(D2DocTagExtra)
    ?: D2DocTagExtra(emptyList())
