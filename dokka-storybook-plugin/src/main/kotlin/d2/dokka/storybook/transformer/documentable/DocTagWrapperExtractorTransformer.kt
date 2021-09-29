package d2.dokka.storybook.transformer.documentable

import d2.dokka.storybook.model.doc.D2DocTagExtra
import d2.dokka.storybook.model.doc.docTagWrappers
import org.jetbrains.dokka.model.DAnnotation
import org.jetbrains.dokka.model.DClass
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DEnum
import org.jetbrains.dokka.model.DInterface
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DObject
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.transformers.documentation.DocumentableTransformer

class DocTagWrapperExtractorTransformer: DocumentableTransformer {
    override fun invoke(original: DModule, context: DokkaContext): DModule {
        return original.withD2DocTagExtras()
    }

    private fun DModule.withD2DocTagExtras() = copy(
        extra = extra + this.toD2DocTagExtra(),
        packages = packages.map { it.withD2DocTagExtras() }
    )

    private fun DPackage.withD2DocTagExtras() = copy(
        extra = extra + this.toD2DocTagExtra(),
        classlikes = classlikes.map { it.withD2DocTagExtras() },
        typealiases = typealiases.map { it.withD2DocTagExtras() }
    )

    private fun DClasslike.withD2DocTagExtras(): DClasslike = when (this) {
        is DClass -> withD2DocTagExtras()
        is DEnum -> withD2DocTagExtras()
        is DInterface -> withD2DocTagExtras()
        is DObject -> withD2DocTagExtras()
        is DAnnotation -> withD2DocTagExtras()
    }

    private fun DClass.withD2DocTagExtras() = copy(
        extra = extra + this.toD2DocTagExtra(),
        properties = properties.map { it.withD2DocTagExtras() }
    )

    private fun DEnum.withD2DocTagExtras() = copy(
        extra = extra + this.toD2DocTagExtra(),
        properties = properties.map { it.withD2DocTagExtras() }
    )

    private fun DInterface.withD2DocTagExtras() = copy(
        extra = extra + this.toD2DocTagExtra(),
        properties = properties.map { it.withD2DocTagExtras() }
    )

    private fun DObject.withD2DocTagExtras() = copy(
        extra = extra + this.toD2DocTagExtra(),
        properties = properties.map { it.withD2DocTagExtras() }
    )

    private fun DAnnotation.withD2DocTagExtras() = copy(
        extra = extra + this.toD2DocTagExtra(),
        properties = properties.map { it.withD2DocTagExtras() }
    )

    private fun DProperty.withD2DocTagExtras() = copy(
        extra = extra + this.toD2DocTagExtra()
    )

    private fun DTypeAlias.withD2DocTagExtras() = copy(
        extra = extra + this.toD2DocTagExtra(),
    )

    private fun Documentable.toD2DocTagExtra(): D2DocTagExtra {
        val (_, d2DocTagWrappers) = documentation.docTagWrappers()
        return D2DocTagExtra(d2DocTagWrappers)
    }
}