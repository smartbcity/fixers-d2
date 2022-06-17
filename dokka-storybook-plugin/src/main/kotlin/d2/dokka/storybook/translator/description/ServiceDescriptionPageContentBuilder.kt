package d2.dokka.storybook.translator.description

import d2.dokka.storybook.model.Constants
import d2.dokka.storybook.model.doc.DocumentableIndexes
import d2.dokka.storybook.model.doc.utils.directAnnotation
import d2.dokka.storybook.model.doc.utils.f2FunctionType
import d2.dokka.storybook.model.doc.utils.isCommand
import d2.dokka.storybook.model.doc.utils.isF2Command
import d2.dokka.storybook.model.render.D2TextStyle
import d2.dokka.storybook.model.doc.utils.documentableIn
import d2.dokka.storybook.model.doc.utils.isF2
import d2.dokka.storybook.model.doc.utils.isF2Consumer
import d2.dokka.storybook.model.doc.utils.isF2Function
import d2.dokka.storybook.model.doc.utils.isF2Supplier
import d2.dokka.storybook.model.doc.utils.toTypeString
import d2.dokka.storybook.translator.block
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.ArrayValue
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.LiteralValue
import org.jetbrains.dokka.model.Projection
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.TextStyle

internal abstract class ServiceDescriptionPageContentBuilder(
    private val isLeft: Boolean,
    private val contentBuilder: PageContentBuilder,
    override val documentableIndexes: DocumentableIndexes
): DescriptionPageContentBuilder() {

    override fun contentFor(d: Documentable): ContentNode? {
        return when (d) {
            is DClasslike -> contentFor(d)
            else -> null
        }
    }

    fun contentFor(c: DClasslike): ContentNode {
        return contentBuilder.contentFor(c) {
            group(kind = ContentKind.Cover) {
                header(c.headerLevel(), if (isLeft) "Commands" else "Queries")
                +contentForDescription(c)
            }
            group(styles = setOf(ContentStyle.TabbedContent)) {
                +contentForComments(c)
                functionsBlock(c.functions)
            }
        }
    }

    private fun PageContentBuilder.DocumentableContentBuilder.functionsBlock(
        functions: Collection<DFunction>,
    ) {
        val displayedFunctions = functions.filter { it.isCommand() == isLeft }
        block(kind = ContentKind.Properties, elements = displayedFunctions) { function ->
            functionSignature(function)
            text("<br/>")
            functionAccess(function)

            group(setOf(function.dri), function.sourceSets.toSet(), ContentKind.Main) {
                function.documentation.forEach { (_, docNode) ->
                    docNode.children.firstOrNull()?.root?.let {
                        group(kind = ContentKind.Comment) {
                            comment(it)
                        }
                    }
                }
            }
        }
    }

    private fun PageContentBuilder.DocumentableContentBuilder.functionSignature(function: DFunction) {
        val signature = FunctionSignature.of(function)
        text(signature.name, styles = setOf(TextStyle.Bold))
        text("(")
        signature.params.forEachIndexed { i, (name, type) ->
            val separator = if (i > 0) ", " else ""
            text("$separator$name:")
            type(type)
        }
        text(")")
        signature.returnType?.let {
            text(":")
            type(it)
        }
    }

    private fun PageContentBuilder.DocumentableContentBuilder.type(type: Projection)  {
        val typeDocumentable = type.documentableIn(documentableIndexes.documentables)
        if (typeDocumentable == null) {
            text(type.toTypeString(), styles = setOf(D2TextStyle.Code))
        } else {
            link(text = type.toTypeString(), address = typeDocumentable.dri, styles = setOf(D2TextStyle.Code))
        }
    }

    private fun PageContentBuilder.DocumentableContentBuilder.functionAccess(function: DFunction) {
        function.directAnnotation(Constants.Annotation.PERMIT_ALL)
            ?.let { text("Access: public", styles = setOf(TextStyle.Italic)) }

        function.directAnnotation(Constants.Annotation.ROLES_ALLOWED)
            ?.let { annotation ->
                val roles = (annotation.params["value"] as? ArrayValue)
                    ?.value
                    .orEmpty()
                    .map { it as LiteralValue }
                    .map(LiteralValue::text)
                text("Access: ${roles.joinToString(", ")}", styles = setOf(TextStyle.Italic))
            }
    }

    private data class FunctionSignature(
        val name: String,
        val params: List<Pair<String, Projection>>,
        val returnType: Projection?
    ) {
        companion object {
            fun of(function: DFunction): FunctionSignature {
                if (function.type.isF2()) {
                    val functionType = function.f2FunctionType()
                    val paramName = if (function.isF2Command()) "cmd" else "query"

                    return when {
                        functionType.isF2Consumer() -> FunctionSignature(
                            name = function.name,
                            params = listOf(paramName to functionType.projections.first()),
                            returnType = null,
                        )
                        functionType.isF2Function() -> FunctionSignature(
                            name = function.name,
                            params = listOf(paramName to functionType.projections.first()),
                            returnType = functionType.projections.last(),
                        )
                        functionType.isF2Supplier() -> FunctionSignature(
                            name = function.name,
                            params = emptyList(),
                            returnType = functionType.projections.first(),
                        )
                        else -> FunctionSignature(
                            name = function.name,
                            params = emptyList(),
                            returnType = null,
                        )
                    }
                }

                return FunctionSignature(
                    name = function.name,
                    params = function.parameters.map { param -> param.name.orEmpty() to param.type },
                    returnType = function.receiver?.type
                )
            }
        }
    }
}
