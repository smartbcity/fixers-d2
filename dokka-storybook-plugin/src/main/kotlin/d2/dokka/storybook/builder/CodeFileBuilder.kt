package d2.dokka.storybook.builder

import d2.dokka.storybook.model.code.imports.CodeImport

abstract class CodeFileBuilder<B: Appendable> {
    protected abstract val builder: B

    protected open val imports: MutableList<CodeImport> = mutableListOf()

    @Suppress("MagicNumber")
    open val indentSize: Int = 4

    open fun write(doWrite: B.() -> Unit) = builder.doWrite()

    open fun append(value: String, indentLevel: Int = 0) {
        if (indentLevel > 0) {
            appendIndent(indentLevel)
        }
        builder.append(value)
    }

    open fun appendIndent(level: Int) {
        append(" ".repeat(level * indentSize))
    }

    open fun addImport(path: String, element: String, isComposite: Boolean = false) {
        addImport(CodeImport(
            path = path,
            element = element,
            isComposite = isComposite
        ))
    }

    open fun addImport(import: CodeImport) {
        imports.add(import)
    }

    abstract fun build(): String
}
