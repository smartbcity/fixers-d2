package d2.dokka.storybook.model.code.react

import d2.dokka.storybook.model.code.CodeElement
import d2.dokka.storybook.model.code.WithImport
import d2.dokka.storybook.model.code.WithParams

sealed interface ReactNode: CodeElement

interface ReactComponent: ReactNode, WithImport, WithParams

data class StringNode(override val identifier: String): ReactNode {
    override fun toString() = "\"$identifier\""
}
