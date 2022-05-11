package d2.dokka.storybook.translator.description

import d2.dokka.storybook.model.doc.DocumentableIndexes
import d2.dokka.storybook.model.render.D2TextStyle
import d2.dokka.storybook.model.render.documentableIn
import d2.dokka.storybook.model.render.isF2
import d2.dokka.storybook.model.render.isF2Consumer
import d2.dokka.storybook.model.render.isF2Function
import d2.dokka.storybook.model.render.isF2Supplier
import d2.dokka.storybook.model.render.toTypeString
import d2.dokka.storybook.translator.block
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.GenericTypeConstructor
import org.jetbrains.dokka.model.Projection
import org.jetbrains.dokka.model.TypeAliased
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.TextStyle

internal abstract class ServiceDescriptionPageContentBuilder(
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
                buildTitle(c)
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
        block(kind = ContentKind.Properties, elements = functions) { function ->
            functionSignature(FunctionSignature.of(function))

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

    private fun PageContentBuilder.DocumentableContentBuilder.functionSignature(function: FunctionSignature) {
        text(function.name, styles = setOf(TextStyle.Bold))
        text("(")
        function.params.forEachIndexed { i, (name, type) ->
            val separator = if (i > 0) ", " else ""
            text("$separator$name:")
            type(type)
        }
        text(")")
        function.returnType?.let {
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

    private data class FunctionSignature(
        val name: String,
        val params: List<Pair<String, Projection>>,
        val returnType: Projection?
    ) {
        companion object {
            fun empty(name: String) = FunctionSignature(
                name = name,
                params = emptyList(),
                returnType = null
            )

            fun of(function: DFunction): FunctionSignature {
                if (function.type.isF2()) {
                    val functionType = function.type as? GenericTypeConstructor
                        ?: (function.type as TypeAliased).inner as GenericTypeConstructor

                    return when {
                        functionType.isF2Consumer() -> FunctionSignature(
                            name = function.name,
                            params = listOf("cmd" to functionType.projections.first()),
                            returnType = null
                        )
                        functionType.isF2Function() -> FunctionSignature(
                            name = function.name,
                            params = listOf("cmd" to functionType.projections.first()),
                            returnType = functionType.projections.last()
                        )
                        functionType.isF2Supplier() -> FunctionSignature(
                            name = function.name,
                            params = emptyList(),
                            returnType = functionType.projections.first()
                        )
                        else -> empty(function.name)
                    }
                }

                return FunctionSignature(
                    name = function.name,
                    params = function.parameters.map { param -> param.name.orEmpty() to param.type },
                    returnType = null
                )
            }
        }
    }


}
