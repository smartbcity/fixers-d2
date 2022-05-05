package d2.dokka.storybook.model.doc

import com.intellij.util.containers.BidirectionalMap
import d2.dokka.storybook.model.doc.tag.Child
import d2.dokka.storybook.model.doc.tag.Parent
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Documentable

data class DocumentableIndexes(
    val documentables: Map<DRI, Documentable>,
    val childToParentBiMap: BidirectionalMap<DRI, DRI>
) {
    companion object {
        fun from(documentables: List<Documentable>): DocumentableIndexes {
            val documentablesIndex = documentables.associateBy(Documentable::dri).toMutableMap()
            val childToParentBiMap = BidirectionalMap<DRI, DRI>()

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

            childToParentBiMap.putAll(childToParentMap)

            return DocumentableIndexes(
                documentables = documentablesIndex,
                childToParentBiMap = childToParentBiMap
            )
        }
    }
}
