package d2.dokka.storybook.translator.description

import d2.dokka.storybook.model.Constants
import d2.dokka.storybook.model.doc.DocumentableIndexes
import d2.dokka.storybook.model.doc.tag.D2Type
import d2.dokka.storybook.model.doc.utils.directAnnotation
import d2.dokka.storybook.model.doc.utils.documentableIn
import d2.dokka.storybook.model.doc.utils.f2FunctionType
import d2.dokka.storybook.model.doc.utils.isCommand
import d2.dokka.storybook.model.doc.utils.isF2
import d2.dokka.storybook.model.doc.utils.isF2CommandFunction
import d2.dokka.storybook.model.doc.utils.isF2Consumer
import d2.dokka.storybook.model.doc.utils.isF2Function
import d2.dokka.storybook.model.doc.utils.isF2Supplier
import d2.dokka.storybook.model.doc.utils.isOfType
import d2.dokka.storybook.model.doc.utils.toTypeString
import d2.dokka.storybook.model.render.D2Marker
import d2.dokka.storybook.model.render.D2TextStyle
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
    private val displayType: FunctionDisplayType,
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
            val (commands, queries) = c.functions.partition { it.isCommand(documentableIndexes.documentables) }
            text("", kind = D2Marker.Spacer)
            contentFor(c, queries, "Queries")
            text("", kind = D2Marker.Spacer)
            contentFor(c, commands, "Commands")
        }
    }

    private fun PageContentBuilder.DocumentableContentBuilder.contentFor(
        c: DClasslike,
        functions: Collection<DFunction>,
        title: String
    ) {
        if (functions.isNotEmpty()) {
            group(kind = ContentKind.Cover) {
                header(c.headerLevel(), title)
            }
            group(styles = setOf(ContentStyle.TabbedContent)) {
                +contentForComments(c)
                when (displayType) {
                    FunctionDisplayType.HTTP -> httpFunctionsBlock(functions)
                    FunctionDisplayType.KOTLIN -> kotlinFunctionsBlock(functions)
                }
            }
        }
    }

    private fun PageContentBuilder.DocumentableContentBuilder.httpFunctionsBlock(
        functions: Collection<DFunction>,
    ) {
        block(kind = ContentKind.Functions, elements = functions) { function ->
            val signature = FunctionSignature.of(function, documentableIndexes)
            group(setOf(function.dri), function.sourceSets.toSet(), ContentKind.Main) {
                header(4, "POST /${signature.name}")
                functionAccess(function)

                function.documentation.forEach { (_, docNode) ->
                    docNode.children.firstOrNull()?.root?.let {
                        group(kind = ContentKind.Comment) {
                            comment(it)
                        }
                    }
                }
            }
            group(setOf(function.dri), function.sourceSets.toSet(), ContentKind.Parameters) {
                if (signature.params.isNotEmpty()) {
                    breakLine()
                    text("Body: ")
                }
                signature.params.forEachIndexed { i, (_, type) ->
                    val separator = if (i > 0) ", " else ""
                    text(separator)
                    type(type)
                }
                breakLine()
                if (signature.returnType != null) {
                    text("Result: ")
                    type(signature.returnType)
                }
            }
        }
    }

    private fun PageContentBuilder.DocumentableContentBuilder.kotlinFunctionsBlock(
        functions: Collection<DFunction>,
    ) {
        block(kind = ContentKind.Properties, elements = functions) { function ->
            functionSignature(function)
            text("<br/>")

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
        val signature = FunctionSignature.of(function, documentableIndexes)
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
            text(type.toTypeString(documentableIndexes.documentables), styles = setOf(D2TextStyle.Code))
        } else {
            val linkedDocumentableDri = documentableIndexes.childToParentMap[typeDocumentable.dri]
                ?.takeIf { typeDocumentable.isOfType(D2Type.COMMAND, D2Type.QUERY, D2Type.EVENT, D2Type.RESULT) }
                ?: typeDocumentable.dri

            link(
                text = type.toTypeString(documentableIndexes.documentables),
                address = linkedDocumentableDri,
                styles = setOf(D2TextStyle.Code)
            )
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
            fun of(function: DFunction, documentableIndexes: DocumentableIndexes): FunctionSignature {
                if (function.type.isF2()) {
                    val functionType = function.f2FunctionType()
                    val paramName = if (function.isF2CommandFunction(documentableIndexes.documentables)) "cmd" else "query"

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
                    returnType = function.type.takeUnless { it.toTypeString(documentableIndexes.documentables) == "Unit" },
                )
            }
        }
    }
}

enum class FunctionDisplayType {
    HTTP, KOTLIN
}
