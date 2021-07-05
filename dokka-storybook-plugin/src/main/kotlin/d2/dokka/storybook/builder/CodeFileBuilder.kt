package d2.dokka.storybook.builder

import d2.dokka.storybook.model.CodeImport

abstract class CodeFileBuilder<B> {
    protected abstract val builder: B

    protected open val imports: MutableList<CodeImport> = mutableListOf()

    open fun write(doWrite: B.() -> Unit) = builder.doWrite()

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