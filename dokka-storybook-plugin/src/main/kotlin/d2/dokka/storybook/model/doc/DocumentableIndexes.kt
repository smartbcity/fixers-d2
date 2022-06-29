package d2.dokka.storybook.model.doc

import d2.dokka.storybook.model.doc.tag.Child
import d2.dokka.storybook.model.doc.tag.Parent
import d2.dokka.storybook.model.doc.utils.d2DocTagExtra
import d2.dokka.storybook.model.doc.utils.toRootDocumentable
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Documentable

data class DocumentableIndexes(
    val documentables: Map<DRI, Documentable>,
    val childToParentMap: Map<DRI, DRI>,
    val parentToChildMap: Map<DRI, List<DRI>>
) {
    companion object {
        fun from(documentables: List<Documentable>): DocumentableIndexes {
            val documentablesIndex = documentables.associateBy(Documentable::dri).toMutableMap()

            val childToParentMap = documentables.flatMap { documentable ->
                val parentDri = documentable.d2DocTagExtra()
                    .firstTagOfTypeOrNull<Parent>()
                    ?.target

                val childrenDri = documentable.d2DocTagExtra()
                    .filterTagsOfType<Child>()
                    .mapNotNull(Child::target)

                parentDri?.let { listOf(documentable.dri to it) }
                    .orEmpty()
                    .plus(childrenDri.map { it to documentable.dri })
            }.toMap().toMutableMap()

            documentables.filter { documentable -> documentable.dri !in childToParentMap && documentable !is PageDocumentable }
                .forEach { documentable ->
                    val rootDocumentable = documentable.toRootDocumentable()
                    documentablesIndex[rootDocumentable.dri] = rootDocumentable
                    childToParentMap.putAll(listOf(
                        documentable.dri to rootDocumentable.dri,
                        rootDocumentable.dri to DRI.topLevel
                    ))
                }

            val parentToChildMap = childToParentMap.entries.groupBy({ it.value }, { it.key })

            return DocumentableIndexes(
                documentables = documentablesIndex,
                childToParentMap = childToParentMap,
                parentToChildMap = parentToChildMap
            )
        }
    }
}
