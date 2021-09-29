package d2.dokka.storybook.model.doc.tag

import org.jetbrains.dokka.model.doc.DocTag

data class Parent(override val root: DocTag): WithTarget
