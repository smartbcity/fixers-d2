package d2.dokka.storybook.model.page

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.WithDocumentables

fun PageNode.recursiveDocumentables(): List<Documentable> = documentables()
    .plus(children.flatMap { it.recursiveDocumentables() })

private fun PageNode.documentables() = if (this is WithDocumentables) {
    documentables.filter { it.dri != DRI.topLevel }
} else {
    emptyList()
}
