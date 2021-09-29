package d2.dokka.storybook.model.doc.tag

import org.jetbrains.dokka.model.doc.DocTag

data class Order(override val root: DocTag): WithTextBody {
    val weight = body?.let(String::toInt)
}
