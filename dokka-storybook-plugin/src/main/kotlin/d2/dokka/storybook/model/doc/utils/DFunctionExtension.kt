package d2.dokka.storybook.model.doc.utils

import d2.dokka.storybook.model.doc.tag.D2Type
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.GenericTypeConstructor
import org.jetbrains.dokka.model.TypeAliased

fun DFunction.isCommand(documentables: Map<DRI, Documentable>) = isOfType(D2Type.COMMAND) ||
        parameters.any { it.type.isCommand(documentables) } ||
        isF2CommandFunction(documentables)

fun DFunction.f2FunctionType() = type as? GenericTypeConstructor
    ?: (type as TypeAliased).inner as GenericTypeConstructor

fun DFunction.isF2CommandFunction(documentables: Map<DRI, Documentable>): Boolean {
    return type.isF2() &&
            f2FunctionType().projections.any { it.isCommand(documentables) }
}
