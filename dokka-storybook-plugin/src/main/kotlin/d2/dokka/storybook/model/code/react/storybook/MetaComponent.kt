package d2.dokka.storybook.model.code.react.storybook

import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.imports.CodeImport
import d2.dokka.storybook.model.code.imports.ImportPath
import d2.dokka.storybook.model.code.react.JsonNode
import d2.dokka.storybook.model.code.react.ReactComponent
import d2.dokka.storybook.model.code.react.StringNode

class MetaComponent(
    val title: String
): ReactComponent {
    override val identifier = "Meta"

    override val importData = CodeImport(path = ImportPath.STORYBOOK_BLOCKS, element = identifier, isComposite = true)

    override val params: Map<String, CodeElement> = mapOf(
        ::title.name to StringNode(title),
        "parameters" to JsonNode(
            "previewTabs" to JsonNode(
                "canvas" to JsonNode(
                    "hidden" to true
                )
            )
        )
    )
}
