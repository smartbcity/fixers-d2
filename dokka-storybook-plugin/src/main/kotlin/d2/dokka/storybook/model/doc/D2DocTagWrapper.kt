package d2.dokka.storybook.model.doc

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.WithChildren
import org.jetbrains.dokka.model.doc.CustomTagWrapper
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.firstMemberOfTypeOrNull

sealed class D2DocTagWrapper: WithChildren<DocTag> {
    abstract val root: DocTag
    override val children: List<DocTag>
        get() = root.children
}

sealed class SimpleTextTagWrapper: D2DocTagWrapper() {
    val body: String?
        get() = root.firstMemberOfTypeOrNull<Text>()?.body
}

sealed class SimpleLinkTagWrapper: D2DocTagWrapper() {
    val target: DRI?
        get() = root.firstMemberOfTypeOrNull<DocumentationLink>()?.dri
}

data class D2(override val root: DocTag): SimpleTextTagWrapper()
data class Example(override val root: DocTag): SimpleTextTagWrapper()
data class Parent(override val root: DocTag): SimpleLinkTagWrapper()

fun CustomTagWrapper.toD2DocTagWrapper(): D2DocTagWrapper? {
    return when (name.lowercase()) {
        "d2" -> D2(root = root)
        "example" -> Example(root = root)
        "parent" -> Parent(root = root)
        else -> null
    }
}
