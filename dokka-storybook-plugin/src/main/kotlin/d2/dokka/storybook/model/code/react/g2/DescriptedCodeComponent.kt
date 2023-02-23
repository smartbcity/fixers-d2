package d2.dokka.storybook.model.code.react.g2

import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.imports.CodeImport
import d2.dokka.storybook.model.code.imports.ImportPath
import d2.dokka.storybook.model.code.react.JsonNode
import d2.dokka.storybook.model.code.react.ReactComponent

class DescriptedCodeComponent(
    val leftElement: CodeElement,
    val leftContainerProps: Map<String, String> = emptyMap(),
    val rightElement: CodeElement,
    val rightContainerProps: Map<String, String> = emptyMap(),
): ReactComponent {
    override val identifier: String
        get() = "DescriptedCode"

    override val importData: CodeImport
        get() = CodeImport(path = ImportPath.G2_DOCUMENTATION, element = identifier, isComposite = true)

    override val params: Map<String, CodeElement>
        get() = mapOf(
            ::leftElement.name to leftElement,
            ::leftContainerProps.name to JsonNode(leftContainerProps),
            ::rightElement.name to rightElement,
            ::rightContainerProps.name to JsonNode(rightContainerProps),
        )

    override val children: Collection<CodeElement> = emptyList()
}
