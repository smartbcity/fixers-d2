package d2.dokka.storybook.model.doc.utils

import d2.dokka.storybook.model.doc.tag.D2Type
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.GenericTypeConstructor
import org.jetbrains.dokka.model.Projection
import org.jetbrains.dokka.model.TypeAliased

fun DFunction.isCommand() = isOfType(D2Type.COMMAND) ||
        parameters.any { it.type.isCommand() } ||
        isF2Command()

fun DFunction.f2FunctionType() = type as? GenericTypeConstructor
    ?: (type as TypeAliased).inner as GenericTypeConstructor

fun DFunction.isF2Command(): Boolean {
    return type.isF2() &&
            f2FunctionType().projections.any(Projection::isCommand)
}

private fun Projection.isCommand() = toTypeString().endsWith("Command", true)
