package d2.dokka.storybook.model.code.react

import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.WithImport
import d2.dokka.storybook.model.code.imports.CodeImport
import d2.dokka.storybook.model.code.imports.ImportPath

class CodeHighlighterComponent(
    val displayed: CodeElement,
    val language: String,
    val title: String
): ReactComponent {
    override val identifier: String
        get() = "CodeHighlighter"

    override val importData: CodeImport
        get() = CodeImport(path = ImportPath.G2_DOCUMENTATION, element = identifier, isComposite = true)

    override val params: Map<String, CodeElement>
        get() = mapOf(
            when {
                displayed is StringNode -> "code" to displayed
                displayed is WithImport && displayed.importData.withRawLoader -> "code" to displayed
                else -> "object" to displayed
            },
            ::language.name to StringNode(language),
            ::title.name to StringNode(title)
        )
}
