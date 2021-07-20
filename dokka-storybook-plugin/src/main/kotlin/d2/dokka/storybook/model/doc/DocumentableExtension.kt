package d2.dokka.storybook.model.doc

import org.jetbrains.dokka.model.Documentable

val Documentable.d2Type
    get() = documentation
        .firstD2TagOfTypeOrNull<D2>()
        ?.body
        ?.let(D2Type::get)
