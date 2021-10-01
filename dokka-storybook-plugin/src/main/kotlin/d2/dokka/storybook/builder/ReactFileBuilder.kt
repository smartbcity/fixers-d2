package d2.dokka.storybook.builder

import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.WithImport
import d2.dokka.storybook.model.code.WithParams
import d2.dokka.storybook.model.code.imports.CodeImport
import d2.dokka.storybook.model.code.react.ReactComponent
import d2.dokka.storybook.model.code.react.ReactNode
import d2.dokka.storybook.model.code.react.StringNode

class ReactFileBuilder(
    override val builder: StringBuilder = StringBuilder()
): CodeFileBuilder<StringBuilder>() {

    override val INDENT_SIZE: Int = 2

    override fun build(): String {
        builder.insert(0, buildImports())
        return builder.toString()
    }

    fun append(element: CodeElement, indentLevel: Int = 0) {
        if (element is WithImport) {
            addImport(element.importData)
        }

        when (element) {
            is ReactNode -> append(element, indentLevel)
            else -> append(element.identifier, indentLevel)
        }
    }

    fun appendNewLine() {
        append("\n")
    }

    private fun append(node: ReactNode, indentLevel: Int) {
        when (node) {
            is StringNode -> append(node.toString(), indentLevel)
            is ReactComponent -> appendReactComponent(node, indentLevel)
        }
    }

    private fun appendReactComponent(component: ReactComponent, indentLevel: Int) {
        when {
            component.params.isEmpty() -> appendSimpleComponent(component, indentLevel)
            else -> appendComplexComponent(component, indentLevel)
        }
    }

    private fun appendSimpleComponent(component: ReactComponent, indentLevel: Int) {
        append("<${component.identifier} />", indentLevel)
    }

    private fun appendComplexComponent(component: ReactComponent, indentLevel: Int) {
        append("<${component.identifier}", indentLevel)
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

        append("/>", indentLevel)
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
}
