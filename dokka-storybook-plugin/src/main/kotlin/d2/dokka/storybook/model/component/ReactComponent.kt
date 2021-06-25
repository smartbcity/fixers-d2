package d2.dokka.storybook.model.component

import d2.dokka.storybook.model.FileImport

interface ReactComponent {
    val tagName: String
    val importData: FileImport
    fun params(): Map<String, String>
}