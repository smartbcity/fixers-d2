package d2.dokka.storybook.model.component

import d2.dokka.storybook.model.CodeImport

interface ReactComponent {
    val tagName: String
    val importData: CodeImport
    fun params(): Map<String, String>
}