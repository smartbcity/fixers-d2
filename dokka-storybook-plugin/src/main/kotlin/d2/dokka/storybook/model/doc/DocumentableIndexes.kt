package d2.dokka.storybook.model.doc

import d2.dokka.storybook.model.doc.tag.Child
import d2.dokka.storybook.model.doc.tag.D2Type
import d2.dokka.storybook.model.doc.tag.Parent
import d2.dokka.storybook.model.doc.utils.d2DocTagExtra
import d2.dokka.storybook.model.doc.utils.documentableIn
import d2.dokka.storybook.model.doc.utils.isOfType
import d2.dokka.storybook.model.doc.utils.toRootDocumentable
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.WithSupertypes

data class DocumentableIndexes(
    val documentables: Map<DRI, Documentable>,
    val childToParentMap: Map<DRI, DRI>,
    val parentToChildMap: Map<DRI, List<DRI>>
) {
    companion object {
        fun from(documentables: List<Documentable>): DocumentableIndexes {
            val (inheritedDocumentables, actualDocumentables) = documentables.partition { it.isOfType(D2Type.INHERIT) }

            val documentablesIndex = documentables.associateBy(Documentable::dri).toMutableMap()

            inheritedDocumentables.forEach { documentable ->
                documentablesIndex[documentable.dri] = documentable.supertypeIn(documentablesIndex)
            }

            val childToParentMap = actualDocumentables.flatMap { documentable ->
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

            actualDocumentables.filter { documentable -> documentable.dri !in childToParentMap && documentable !is PageDocumentable }
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

        @Suppress("ThrowsCount")
        private fun <T: Documentable> T.supertypeIn(index: Map<DRI, Documentable>): Documentable {
            val superDocumentables = when (this) {
                is WithSupertypes -> supertypes.values.flatten().mapNotNull { it.typeConstructor.documentableIn(index) }
                is DTypeAlias -> underlyingType.values.mapNotNull { it.documentableIn(index) }
                else -> throw IllegalArgumentException("'inherit' d2 type is not supported for $dri")
            }

            return when (superDocumentables.size) {
                0 -> throw IllegalArgumentException("$dri is tagged with 'inherit' but none of its supertypes are tagged with @d2")
                1 -> superDocumentables.first()
                else -> throw IllegalArgumentException(
                    "$dri is tagged with 'inherit' but more than one of its supertypes are tagged with @d2"
                )
            }
        }
    }
}
