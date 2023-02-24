package d2.dokka.storybook.model.doc.tag

import org.jetbrains.dokka.model.doc.DocTag

data class Default(override val root: DocTag): D2DocTagWrapper, WithTextBody
