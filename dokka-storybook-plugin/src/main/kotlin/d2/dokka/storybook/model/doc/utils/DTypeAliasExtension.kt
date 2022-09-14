package d2.dokka.storybook.model.doc.utils

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.GenericTypeConstructor

fun DTypeAlias.isF2CommandFunction(documentables: Map<DRI, Documentable>): Boolean {
    val aliasedType = underlyingType.values.first()
    return aliasedType.isF2() &&
            aliasedType is GenericTypeConstructor &&
            aliasedType.projections.any { it.isCommand(documentables) }
}
