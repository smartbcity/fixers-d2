package d2.dokka.storybook.model.doc.tag

import org.jetbrains.dokka.model.doc.DocTag

data class Ref(override val root: DocTag): D2DocTagWrapper, WithTarget
