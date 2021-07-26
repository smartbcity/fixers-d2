package d2.dokka.storybook.model.doc

import org.jetbrains.dokka.links.sureClassNames
import org.jetbrains.dokka.model.Documentable

val Documentable.d2Type
    get() = documentation
        .firstD2TagOfTypeOrNull<D2>()
        ?.body
        ?.let(D2Type::get)

fun Documentable.toRootDocumentable() = RootDocumentable(
    name = "${name.orEmpty()}Page",
    dri = dri.copy(classNames = "${dri.sureClassNames}Page"),
    documentation = documentation,
    sourceSets = sourceSets,
    expectPresentInSet = expectPresentInSet,
    children = listOf(this)
)

val Documentable.title
    get() = when (this) {
        is RootDocumentable -> pageDocumentation?.title?.body ?: name.removeSuffix("Page")
        else -> documentation.firstD2TagOfTypeOrNull<Title>()?.body ?: name!!
    }
