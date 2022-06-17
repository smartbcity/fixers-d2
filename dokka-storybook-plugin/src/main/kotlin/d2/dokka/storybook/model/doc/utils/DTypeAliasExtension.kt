package d2.dokka.storybook.model.doc.utils

import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.GenericTypeConstructor
import org.jetbrains.dokka.model.Projection

fun DTypeAlias.isF2CommandFunction(): Boolean {
    val aliasedType = underlyingType.values.first()
    return aliasedType.isF2() &&
            aliasedType is GenericTypeConstructor &&
            aliasedType.projections.any(Projection::isCommand)
}
