package d2.dokka.storybook.model.code.react.g2

import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.imports.CodeImport
import d2.dokka.storybook.model.code.imports.ImportPath
import d2.dokka.storybook.model.code.react.ReactComponent

class DescriptedCodeComponent(
    val leftElement: CodeElement,
    val rightElement: CodeElement,
): ReactComponent {
    override val identifier: String
        get() = "DescriptedCode"

    override val importData: CodeImport
        get() = CodeImport(path = ImportPath.G2_DOCUMENTATION, element = identifier, isComposite = true)

    override val params: Map<String, CodeElement>
        get() = mapOf(
            ::leftElement.name to leftElement,
            ::rightElement.name to rightElement
        )
}
