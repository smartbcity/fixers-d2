package d2.dokka.storybook.model.doc.utils

import d2.dokka.storybook.model.doc.tag.D2Type
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.driOrNull
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Bound
import org.jetbrains.dokka.model.Contravariance
import org.jetbrains.dokka.model.Covariance
import org.jetbrains.dokka.model.DefinitelyNonNullable
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.Dynamic
import org.jetbrains.dokka.model.Invariance
import org.jetbrains.dokka.model.JavaObject
import org.jetbrains.dokka.model.Nullable
import org.jetbrains.dokka.model.PrimitiveJavaType
import org.jetbrains.dokka.model.Projection
import org.jetbrains.dokka.model.Star
import org.jetbrains.dokka.model.TypeAliased
import org.jetbrains.dokka.model.TypeConstructor
import org.jetbrains.dokka.model.TypeParameter
import org.jetbrains.dokka.model.UnresolvedBound
import org.jetbrains.dokka.model.Variance
import org.jetbrains.dokka.model.Void

fun Projection.toTypeString(documentables: Map<DRI, Documentable>): String {
    return when (this) {
        is Bound -> toTypeString(documentables)
        is Star -> "*"
        is Covariance<*> -> "out ${inner.toTypeString(documentables)}"
        is Contravariance<*> -> "in ${inner.toTypeString(documentables)}"
        is Invariance<*> -> inner.toTypeString(documentables)
    }
}

private fun Bound.toTypeString(documentables: Map<DRI, Documentable>): String {
    return when (this) {
        is TypeParameter -> dri.classNames ?: "Unknown"
        is TypeConstructor -> toTypeString(documentables)
        is Nullable -> "${inner.toTypeString(documentables)}?"
        is TypeAliased -> typeAlias.toTypeString(documentables)
        is PrimitiveJavaType -> name
        is Void -> "Unit"
        is JavaObject -> "JavaObject"
        is Dynamic -> "Dynamic"
        is UnresolvedBound -> name
        is DefinitelyNonNullable -> inner.toTypeString(documentables)
    }
}

private fun TypeConstructor.toTypeString(documentables: Map<DRI, Documentable>): String {
    val typeName = documentables[dri]?.name
        ?: dri.classNames
        ?: "Unknown"

    if (projections.isEmpty()) {
        return typeName
    }
    val projectionNames = this.projections.joinToString(", ") { it.toTypeString(documentables) }
    return "$typeName<$projectionNames>"
}

fun Projection.documentableIn(documentables: Map<DRI, Documentable>): Documentable? {
    return when (this) {
        is Bound -> documentableIn(documentables)
        is Star -> null
        is Variance<*> -> inner.documentableIn(documentables)
    }
}

private fun Bound.documentableIn(documentables: Map<DRI, Documentable>): Documentable? {
    return when (this) {
        is TypeParameter -> documentables[dri]
        is TypeConstructor -> documentables[driOrNull] ?: projections.firstNotNullOfOrNull { it.documentableIn(documentables) }
        is Nullable -> inner.documentableIn(documentables)
        is TypeAliased -> typeAlias.documentableIn(documentables) ?: inner.documentableIn(documentables)
        else -> null
    }
}

private object BoundTypes {
    val COLLECTION = listOf("kotlin.collections/", "/Array///")
    const val MAP = "Map///"

    object F2 {
        const val PACKAGE_FNC = "f2.dsl.fnc"
        const val CONSUMER = "$PACKAGE_FNC/F2Consumer"
        const val FUNCTION = "$PACKAGE_FNC/F2Function"
        const val SUPPLIER = "$PACKAGE_FNC/F2Supplier"
    }
}

fun Bound.safeDri() = driOrNull?.toString().orEmpty()

fun Bound.isCollection(): Boolean {
    val driString = safeDri()
    return BoundTypes.COLLECTION.any(driString::contains)
}
fun Bound.isMap() = driContains(BoundTypes.MAP)
fun Bound.isF2Consumer() = driContains(BoundTypes.F2.CONSUMER)
fun Bound.isF2Function() = driContains(BoundTypes.F2.FUNCTION)
fun Bound.isF2Supplier() = driContains(BoundTypes.F2.SUPPLIER)
fun Bound.isF2() = isF2Consumer() || isF2Function() || isF2Supplier()

private fun Bound.driContains(str: String): Boolean = when (this) {
    is TypeAliased -> inner.driContains(str)
    else -> safeDri().contains(str)
}

fun Projection.isCommand(documentables: Map<DRI, Documentable>) = documentableIn(documentables)
    ?.isOfType(D2Type.COMMAND)
    ?:toTypeString(documentables).contains("Command")
