package d2.dokka.storybook.model.doc.tag

import org.jetbrains.dokka.model.doc.DocTag

sealed interface Example: D2DocTagWrapper
data class ExampleText(override val root: DocTag): Example, WithTextBody
data class ExampleLink(override val root: DocTag): Example, WithTarget
