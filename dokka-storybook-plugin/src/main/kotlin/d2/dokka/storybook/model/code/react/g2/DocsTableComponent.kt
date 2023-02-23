package d2.dokka.storybook.model.code.react.g2

import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.imports.CodeImport
import d2.dokka.storybook.model.code.imports.ImportPath
import d2.dokka.storybook.model.code.react.LiteralNode
import d2.dokka.storybook.model.code.react.ReactComponent
import d2.dokka.storybook.model.code.react.html.SimpleHtmlElement

class DocsTableComponent(
    blocks: Collection<CodeElement>,
    gridColumnNumber: Int = 2,
): ReactComponent {
    override val identifier: String = "DocsTable"
    override val importData: CodeImport = CodeImport(path = ImportPath.G2_DOCUMENTATION, element = identifier, isComposite = true)
    override val params: Map<String, CodeElement> = mapOf("gridColumnNumber" to LiteralNode("$gridColumnNumber"))
    override val children: Collection<CodeElement> = blocks.map { SimpleHtmlElement("div", listOf(it)) }
}
