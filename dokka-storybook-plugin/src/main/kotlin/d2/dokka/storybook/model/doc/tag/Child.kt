package d2.dokka.storybook.model.doc.tag

import org.jetbrains.dokka.model.doc.DocTag

data class Child(override val root: DocTag): WithTarget
