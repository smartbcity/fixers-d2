package d2.dokka.storybook.model.code.react

import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.WithImport
import d2.dokka.storybook.model.code.WithParams

sealed interface ReactNode: CodeElement

interface ReactComponent: ReactNode, WithImport, WithParams

open class LiteralNode(override val identifier: String): ReactNode
class StringNode(identifier: String): LiteralNode("\"$identifier\"")
object NullNode: LiteralNode("null")

class JsonNode(vararg properties: Pair<String, Any>): ReactNode, WithParams {
    override val identifier: String = ""
    override val params: Map<String, CodeElement> = properties.toMap()
        .mapValues { (_, value) -> value.toCodeElement()}

    constructor(map: Map<*, *>): this(*map
        .map { (key, value) -> key.toString() to value.toCodeElement() }
        .toTypedArray()
    )
}

data class ArrayNode(private val collection: Iterable<*>): ReactNode {
    override val identifier: String = ""
    val elements = collection.map(Any?::toCodeElement)
}

private fun Any?.toCodeElement() = when (this) {
    is CodeElement -> this
    null -> NullNode
    is String -> StringNode(this)
    is Number -> LiteralNode(toString())
    is Boolean -> LiteralNode(toString())
    is Array<*> -> ArrayNode(asList())
    is Iterable<*> -> ArrayNode(this)
    is Map<*, *> -> JsonNode(this)
    else -> throw NotImplementedError("Unsupported CodeElement type: ${javaClass.name}")
}
