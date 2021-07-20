package d2.dokka.storybook.model.doc

import org.jetbrains.dokka.model.SourceSetDependent
import org.jetbrains.dokka.model.doc.CustomTagWrapper
import org.jetbrains.dokka.model.doc.DocumentationNode
import org.jetbrains.dokka.model.doc.TagWrapper

fun SourceSetDependent<DocumentationNode>.docTagWrappers(): Pair<List<TagWrapper>, List<D2DocTagWrapper>> {
    val (customTagWrappers, dokkaTagWrappers) = flatMap { (_, docNode) -> docNode.children }
        .partition { it is CustomTagWrapper }

    val d2TagWrappers = customTagWrappers
        .map { it as CustomTagWrapper }
        .mapNotNull(CustomTagWrapper::toD2DocTagWrapper)

    return dokkaTagWrappers to d2TagWrappers
}

inline fun <reified T: D2DocTagWrapper> SourceSetDependent<DocumentationNode>.firstD2TagOfType(): T {
    return docTagWrappers().second.filterIsInstance<T>().first()
}

inline fun <reified T: D2DocTagWrapper> SourceSetDependent<DocumentationNode>.firstD2TagOfTypeOrNull(): T? {
    return docTagWrappers().second.filterIsInstance<T>().firstOrNull()
}

inline fun <reified T: TagWrapper> SourceSetDependent<DocumentationNode>.firstDokkaTagOfType(): T {
    return docTagWrappers().first.filterIsInstance<T>().first()
}

inline fun <reified T: TagWrapper> SourceSetDependent<DocumentationNode>.firstDokkaTagOfTypeOrNull(): T? {
    return docTagWrappers().first.filterIsInstance<T>().firstOrNull()
}
