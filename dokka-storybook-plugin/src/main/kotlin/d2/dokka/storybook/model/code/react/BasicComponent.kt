package d2.dokka.storybook.model.code.react

import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.imports.CodeImport

open class BasicComponent(
    override val importData: CodeImport,
    override val identifier: String = importData.element
): ReactComponent {
    override val params: Map<String, CodeElement> = emptyMap()
}