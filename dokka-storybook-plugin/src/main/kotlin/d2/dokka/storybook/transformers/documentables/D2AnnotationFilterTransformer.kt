package d2.dokka.storybook.transformers.documentables

import d2.dsl.annotation.D2
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Annotations
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.properties.WithExtraProperties
import org.jetbrains.dokka.transformers.documentation.PreMergeDocumentableTransformer
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

class D2AnnotationFilterTransformer: PreMergeDocumentableTransformer {
    companion object {
        val D2_DRI = DRI(
            packageName = D2::class.java.packageName,
            classNames = D2::class.java.simpleName
        )
    }

    override fun invoke(modules: List<DModule>): List<DModule> {
        return modules.map { module ->
            module.onlyD2AnnotatedClasses()
        }
    }

    private fun DModule.onlyD2AnnotatedClasses(): DModule {
        val annotatedPackages = this.packages.map { pack ->
            pack.onlyD2AnnotatedClasses()
        }
        return this.copy(packages = annotatedPackages)
    }

    private fun DPackage.onlyD2AnnotatedClasses(): DPackage {
        val annotatedClassLikes = this.classlikes.filter { classLike ->
            classLike.isAnnotatedWithD2()
        }
        return this.copy(classlikes = annotatedClassLikes)
    }

    private fun DClasslike.isAnnotatedWithD2(): Boolean {
        return this.safeAs<WithExtraProperties<out Documentable>>()
            ?.isAnnotatedWithD2()
            ?: false
    }

    private fun WithExtraProperties<out Documentable>.isAnnotatedWithD2(): Boolean {
        return this.extra[Annotations]?.directAnnotations
            ?.flatMap { it.value }
            ?.any { annotation -> annotation.isD2() }
            ?: false
    }

    private fun Annotations.Annotation.isD2(): Boolean {
        return this.dri.toString() == D2_DRI.toString()
    }
}
