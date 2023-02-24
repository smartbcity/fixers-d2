package d2.dokka.storybook.model.code.react.g2

import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.imports.CodeImport
import d2.dokka.storybook.model.code.imports.ImportPath
import d2.dokka.storybook.model.code.react.JsonNode
import d2.dokka.storybook.model.code.react.ReactComponent

class AutomateViewerComponent(
    automate: CodeElement
): ReactComponent {
    override val identifier: String = "SsmViewer"
    override val importData: CodeImport = CodeImport(path = ImportPath.G2_S2, element = identifier, isComposite = true)
    override val params: Map<String, CodeElement> = mapOf(
        "automate" to automate,
        "style" to JsonNode(
            "width" to "100%",
            "height" to "300px",
            "border" to "1px solid #eeedea",
            "borderRadius" to "6px",
            "backgroundColor" to "#fffefb"
        )
    )
    override val children: Collection<CodeElement> = emptyList()
}
