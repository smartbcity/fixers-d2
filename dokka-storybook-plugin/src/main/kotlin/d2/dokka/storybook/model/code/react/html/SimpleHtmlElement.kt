package d2.dokka.storybook.model.code.react.html

import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.react.HtmlElement

class SimpleHtmlElement(
    override val identifier: String,
    override val children: Collection<CodeElement> = emptyList(),
    override val params: Map<String, CodeElement> = emptyMap()
): HtmlElement
