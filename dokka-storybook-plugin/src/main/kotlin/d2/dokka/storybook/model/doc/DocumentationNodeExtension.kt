package d2.dokka.storybook.model.doc

import d2.dokka.storybook.model.doc.tag.D2DocTagWrapper
import d2.dokka.storybook.model.doc.tag.toD2DocTagWrapper
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

fun SourceSetDependent<DocumentationNode>.isEmptyDoc() = all { (_, node) -> node.children.isEmpty() }
fun SourceSetDependent<DocumentationNode>.isNotEmptyDoc() = any { (_, node) -> node.children.isNotEmpty() }
