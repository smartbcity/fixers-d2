package d2.dokka.storybook.model.code

import d2.dokka.storybook.model.code.imports.CodeImport

interface WithImport: CodeElement {
    val importData: CodeImport
}
