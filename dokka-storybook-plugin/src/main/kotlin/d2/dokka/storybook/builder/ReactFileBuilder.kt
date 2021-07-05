package d2.dokka.storybook.builder

import d2.dokka.storybook.model.CodeImport
import d2.dokka.storybook.model.component.ReactComponent

class ReactFileBuilder(
    override val builder: StringBuilder = StringBuilder()
): CodeFileBuilder<StringBuilder>() {

    fun appendComponent(component: ReactComponent) {
        addImport(component.importData)
        builder.append("<${component.tagName}\n")
        component.params().map { (key, value) ->
            builder.append("  $key={$value}\n")
        }
        builder.append("/>")
    }

    override fun build(): String {
        builder.insert(0, buildImports())
        return builder.toString()
    }

    private fun buildImports(): String {
        val importBuilder = StringBuilder()

        imports.distinct()
            .sortedBy { import -> "${import.path}///${import.element}" }
            .groupBy(CodeImport::path)
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
}
