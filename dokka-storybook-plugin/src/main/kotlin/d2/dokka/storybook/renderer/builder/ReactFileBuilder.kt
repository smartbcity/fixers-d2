package d2.dokka.storybook.renderer.builder

import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.WithImport
import d2.dokka.storybook.model.code.WithParams
import d2.dokka.storybook.model.code.imports.CodeImport
import d2.dokka.storybook.model.code.react.HtmlElement
import d2.dokka.storybook.model.code.react.JsonNode
import d2.dokka.storybook.model.code.react.LiteralNode
import d2.dokka.storybook.model.code.react.ReactNode

class ReactFileBuilder(
    override val builder: StringBuilder = StringBuilder()
): CodeFileBuilder<StringBuilder>() {

    override val indentSize: Int = 2

    override fun build(): String {
        builder.insert(0, buildImports())
        return builder.toString()
    }

    fun append(element: CodeElement, indentLevel: Int = 0, indentFirstLine: Boolean = true) {
        if (element is WithImport) {
            addImport(element.importData)
        }

        when (element) {
            is ReactNode -> append(element, indentLevel, indentFirstLine)
            else -> append(element.identifier, actualIndentLevel(indentLevel, indentFirstLine))
        }
    }

    fun appendNewLine() {
        append("\n")
    }

    private fun append(node: ReactNode, indentLevel: Int, indentFirstLine: Boolean) {
        when (node) {
            is LiteralNode -> append(node, indentLevel, indentFirstLine)
            is JsonNode -> append(node, indentLevel, indentFirstLine)
            is HtmlElement -> append(node, indentLevel, indentFirstLine)
            else -> throw NotImplementedError("Unsupported react node type: ${node::class.simpleName}")
        }
    }

    private fun append(node: LiteralNode, indentLevel: Int, indentFirstLine: Boolean) {
        append(node.identifier, actualIndentLevel(indentLevel, indentFirstLine))
    }

    private fun append(node: JsonNode, indentLevel: Int, indentFirstLine: Boolean) {
        append("{", actualIndentLevel(indentLevel, indentFirstLine))
        node.params.entries.forEachIndexed { index, (key, value) ->
            appendNewLine()
            append("$key: ", indentLevel + 1)
            when (value) {
                is ReactNode -> append(value, indentLevel + 1, false)
                else -> append(value.toString())
            }
            if (index < node.params.size - 1) {
                append(",")
            }
        }
        if (node.params.isEmpty()) {
            append("}")
        } else {
            appendNewLine()
            append("}", indentLevel)
        }
    }

    private fun append(component: HtmlElement, indentLevel: Int, indentFirstLine: Boolean) {
        append("<${component.identifier}", actualIndentLevel(indentLevel, indentFirstLine))

        if (component.params.isNotEmpty()) {
            appendNewLine()
            component.params.map { (key, value) ->
                append("$key={", indentLevel + 1)
                if (value is WithParams && value.params.isNotEmpty()) {
                    appendNewLine()
                    append(value, indentLevel + 2)
                    appendNewLine()
                    append("}", indentLevel + 1)
                } else {
                    append(value)
                    append("}")
                }
                appendNewLine()
            }
        }

        val closeIndentLevel = indentLevel.takeIf { component.params.isNotEmpty() } ?: 0

        if (component.children.isNotEmpty()) {
            append(">", closeIndentLevel)
            appendNewLine()
            component.children.forEach { child ->
                appendNewLine()
                append(child, minOf(indentLevel + 1, 1)) // markdown interprets 4-spaces indent as code block
                appendNewLine()
            }
            append("</${component.identifier}>", indentLevel)
        } else {
            if (component.params.isEmpty()) {
                append(" ")
            }
            append("/>", closeIndentLevel)
        }
    }

    private fun buildImports(): String {
        val importBuilder = StringBuilder()

        imports.distinct()
            .sortedBy { import -> "${import.element}///${import.path}" }
            .groupBy { import -> import.fullPath() }
            .forEach { (path, imports) ->
                val (compositeImports, globalImports) = imports.partition(CodeImport::isComposite)
                importBuilder.append("import ")

                globalImports.joinTo(importBuilder, ", ", transform = CodeImport::element)

                if (globalImports.isNotEmpty() && compositeImports.isNotEmpty()) {
                    importBuilder.append(", ")
                }

                if (compositeImports.isNotEmpty()) {
                    compositeImports.joinTo(importBuilder, ", ", "{ ", " }", transform = CodeImport::element)
                }

                importBuilder.append(" from '$path'\n")
            }

        importBuilder.append("\n")

        return importBuilder.toString()
    }

    private fun CodeImport.fullPath() = if (withRawLoader) "!!raw-loader!$path" else path

    private fun actualIndentLevel(indentLevel: Int, indentFirstLine: Boolean) = if (indentFirstLine) indentLevel else 0
}
