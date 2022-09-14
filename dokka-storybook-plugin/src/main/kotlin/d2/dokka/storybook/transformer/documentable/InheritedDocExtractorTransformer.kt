package d2.dokka.storybook.transformer.documentable

import d2.dokka.storybook.model.doc.utils.isNotEmptyDoc
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.AdditionalModifiers
import org.jetbrains.dokka.model.DAnnotation
import org.jetbrains.dokka.model.DClass
import org.jetbrains.dokka.model.DEnum
import org.jetbrains.dokka.model.DInterface
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DObject
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.ExtraModifiers
import org.jetbrains.dokka.model.InheritedMember
import org.jetbrains.dokka.model.SourceSetDependent
import org.jetbrains.dokka.model.WithSupertypes
import org.jetbrains.dokka.model.doc.DocumentationNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.transformers.documentation.DocumentableTransformer

class InheritedDocExtractorTransformer: DocumentableTransformer {

    override fun invoke(original: DModule, context: DokkaContext): DModule {
        return original.generateDocumentableMap().let { original.appendInheritedExpectActualDoc(it) }
    }

    private fun DModule.generateDocumentableMap() = getDocumentableEntries().toMap()

    private fun List<Documentable>.getDocumentableEntries() = flatMap { it.getDocumentableEntries() }

    private fun Documentable.getDocumentableEntries(): List<Pair<DRI, Documentable>> {
        return children.getDocumentableEntries()
            .plus(dri to this)
    }

    private fun <T: Documentable> T.appendInheritedExpectActualDoc(documentablesMap: Map<DRI, Documentable>): T {
        @Suppress("UNCHECKED_CAST")
        return when (this) {
            is DModule -> copy(packages = packages.appendInheritedExpectActualDocs(documentablesMap))
            is DPackage -> copy(classlikes = classlikes.appendInheritedExpectActualDocs(documentablesMap))
            is DClass -> copy(
                classlikes = classlikes.appendInheritedExpectActualDocs(documentablesMap),
                properties = properties.appendInheritedExpectActualDocs(documentablesMap),
                functions = functions.appendInheritedExpectActualDocs(documentablesMap)
            )
            is DEnum -> copy(
                classlikes = classlikes.appendInheritedExpectActualDocs(documentablesMap),
                properties = properties.appendInheritedExpectActualDocs(documentablesMap),
                functions = functions.appendInheritedExpectActualDocs(documentablesMap)
            )
            is DInterface -> copy(
                classlikes = classlikes.appendInheritedExpectActualDocs(documentablesMap),
                properties = properties.appendInheritedExpectActualDocs(documentablesMap),
                functions = functions.appendInheritedExpectActualDocs(documentablesMap)
            )
            is DObject -> copy(
                classlikes = classlikes.appendInheritedExpectActualDocs(documentablesMap),
                properties = properties.appendInheritedExpectActualDocs(documentablesMap),
                functions = functions.appendInheritedExpectActualDocs(documentablesMap)
            )
            is DAnnotation -> copy(
                classlikes = classlikes.appendInheritedExpectActualDocs(documentablesMap),
                properties = properties.appendInheritedExpectActualDocs(documentablesMap),
                functions = functions.appendInheritedExpectActualDocs(documentablesMap)
            )
            is DProperty -> copy(
                documentation = closerAncestorNotEmptyDoc(documentablesMap)
            )
            else -> this
        } as T
    }
    private fun <T: Documentable> List<T>.appendInheritedExpectActualDocs(
        documentablesMap: Map<DRI, Documentable>
    ) = map { it.appendInheritedExpectActualDoc(documentablesMap) }

    private fun DProperty.closerAncestorNotEmptyDoc(
        documentablesMap: Map<DRI, Documentable>
    ): SourceSetDependent<DocumentationNode> {
        return documentation.takeIf { it.isNotEmptyDoc() }
            ?: supertype(documentablesMap)?.closerAncestorNotEmptyDoc(documentablesMap)
            ?: documentation
    }

    private fun DProperty.supertype(documentablesMap: Map<DRI, Documentable>): DProperty? {
        return supertypes(documentablesMap).firstOrNull() as? DProperty
    }

    private fun <T: Documentable> T.supertypes(documentablesMap: Map<DRI, Documentable>): List<Documentable> {
        return when (this) {
            is WithSupertypes -> supertypes.values.flatten().mapNotNull { documentablesMap[it.typeConstructor.dri] }
            is DProperty -> {
                val inheritedFromDri = extra[InheritedMember.Companion]
                    ?.inheritedFrom
                    ?.values
                    ?.firstOrNull()

                listOfNotNull(documentablesMap[inheritedFromDri]).ifEmpty {
                    if (!isOverride()) return@ifEmpty emptyList()

                    documentablesMap[dri.copy(callable = null)]
                        ?.supertypes(documentablesMap)
                        .orEmpty()
                        .mapNotNull { parent -> documentablesMap[parent.dri.copy(callable = dri.callable)] }
                }
            }
            else -> emptyList()
        }
    }

    private fun DProperty.isOverride() = extra[AdditionalModifiers.Companion]
        ?.content
        .orEmpty()
        .values
        .flatten()
        .contains(ExtraModifiers.KotlinOnlyModifiers.Override)
}
