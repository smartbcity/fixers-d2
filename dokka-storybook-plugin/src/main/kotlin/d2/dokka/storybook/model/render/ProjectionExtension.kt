package d2.dokka.storybook.model.render

import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.driOrNull
import org.jetbrains.dokka.model.Bound
import org.jetbrains.dokka.model.Contravariance
import org.jetbrains.dokka.model.Covariance
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
import org.jetbrains.dokka.model.Void

fun Projection.toTypeString(): String {
    return when (this) {
        is Bound -> toTypeString()
        is Star -> "*"
        is Covariance<*> -> "out ${inner.toTypeString()}"
        is Contravariance<*> -> "in ${inner.toTypeString()}"
        is Invariance<*> -> inner.toTypeString()
    }
}

private fun Bound.toTypeString(): String {
    return when (this) {
        is TypeParameter -> dri.classNames.orUnknown()
        is TypeConstructor -> toTypeString()
        is Nullable -> "${inner.toTypeString()}?"
        is TypeAliased -> typeAlias.toTypeString()
        is PrimitiveJavaType -> name
        is Void -> "Unit"
        is JavaObject -> "JavaObject"
        is Dynamic -> "Dynamic"
        is UnresolvedBound -> name
    }
}

private fun TypeConstructor.toTypeString(): String {
    val typeName = dri.classNames.orUnknown()
    if (projections.isEmpty()) {
        return typeName
    }
    val projectionNames = this.projections.joinToString(", ", transform = Projection::toTypeString)
    return "$typeName<$projectionNames>"
}

private fun String?.orUnknown() = this ?: "Unknown"

fun Bound.isCollection() = this.driOrNull?.toString().orEmpty().contains("kotlin.collections/")
fun Bound.isMap() = this.driOrNull?.toString().orEmpty().contains("Map///")