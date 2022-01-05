package d2.dokka.storybook.model.code

import d2.dokka.storybook.model.code.imports.CodeImport

class BasicImportedElement(
    override val importData: CodeImport,
    override val identifier: String = importData.element
): WithImport
