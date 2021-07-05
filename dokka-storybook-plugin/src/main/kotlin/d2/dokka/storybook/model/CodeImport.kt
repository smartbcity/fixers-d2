package d2.dokka.storybook.model

data class CodeImport(
    val path: String,
    val element: String,
    val isComposite: Boolean = false
)
