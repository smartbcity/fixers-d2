package d2.dokka.storybook.model.component

import d2.dokka.storybook.model.CodeImport

class DescriptedCodeComponent(
    val description: String,
    val code: String,
) : ReactComponent {
    override val tagName: String
        get() = "DescriptedCode"

    override val importData: CodeImport
        get() = CodeImport(path = "@smartb/archetypes-ui-documentation", element = tagName, isComposite = true)

    override fun params(): Map<String, String> = mapOf(
        ::description.name to description,
        ::code.name to code
    )
}