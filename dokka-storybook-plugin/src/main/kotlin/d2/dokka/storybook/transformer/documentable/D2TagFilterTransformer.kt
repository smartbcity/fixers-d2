package d2.dokka.storybook.transformer.documentable

import d2.dokka.storybook.model.doc.D2DocTagExtra
import d2.dokka.storybook.model.doc.tag.D2
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.properties.WithExtraProperties
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.transformers.documentation.DocumentableTransformer

class D2TagFilterTransformer: DocumentableTransformer {
    override fun invoke(original: DModule, context: DokkaContext): DModule {
        return original.onlyD2TaggedElements()
    }

    private fun DModule.onlyD2TaggedElements(): DModule {
        val taggedPackages = this.packages.map { pack ->
            pack.onlyD2TaggedElements()
        }
        return this.copy(packages = taggedPackages)
    }

    private fun DPackage.onlyD2TaggedElements(): DPackage {
        val taggedClassLikes = this.classlikes.filter { classLike ->
            classLike.isTaggedWithD2()
        }
        val taggedTypeAliases = this.typealiases.filter { typeAlias ->
            typeAlias.isTaggedWithD2()
        }

        return this.copy(
            classlikes = taggedClassLikes,
            typealiases = taggedTypeAliases,
            functions = emptyList(),
            properties = emptyList()
        )
    }

    private fun Documentable.isTaggedWithD2(): Boolean {
        return (this as? WithExtraProperties<Documentable>)
            ?.extra
            ?.get(D2DocTagExtra)
            ?.firstTagOfTypeOrNull<D2>() != null
    }
}
