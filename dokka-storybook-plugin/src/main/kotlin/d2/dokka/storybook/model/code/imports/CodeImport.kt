package d2.dokka.storybook.model.code.imports

data class CodeImport(
    val path: String,
    val element: String,
    val isComposite: Boolean = false,
    val withRawLoader: Boolean = false
)
