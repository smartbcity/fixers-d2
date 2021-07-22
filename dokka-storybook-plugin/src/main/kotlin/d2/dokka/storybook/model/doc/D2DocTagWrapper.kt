package d2.dokka.storybook.model.doc

import org.jetbrains.dokka.base.parsers.MarkdownParser
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.WithChildren
import org.jetbrains.dokka.model.childrenOfType
import org.jetbrains.dokka.model.doc.CustomTagWrapper
import org.jetbrains.dokka.model.doc.Description
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.P
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.firstMemberOfType
import org.jetbrains.dokka.model.firstMemberOfTypeOrNull
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

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
data class Example(override val root: DocTag): D2DocTagWrapper() {
    val body: String
        get() = root.firstMemberOfType<P>()
            .childrenOfType<Text>() // text between [] is parsed as a reference and stored in a dedicated Text tag wrapper
            .joinToString("", transform = Text::hrefOrBody)
}
data class Parent(override val root: DocTag): SimpleLinkTagWrapper()

data class Page(
    override val root: DocTag
): D2DocTagWrapper() {
    val description: Description?
    val example: Example?

    init {
        val childrenTags = root.children.flatMap { tag ->
            tag.childrenOfType<Text>().flatMap { text ->
                text.hrefOrBody()
                    .replace("@@", "\n@")
                    .split("\n")
                    .filter(String::isNotBlank)
            }
        }.joinToString("\n")

        val tags = MarkdownParser({ null }, null)
            .parse(childrenTags)
            .children
            .map { child ->
                if (child is CustomTagWrapper) child.toD2DocTagWrapper() ?: child else child
            }

        description = tags.firstIsInstanceOrNull()
        example = tags.firstIsInstanceOrNull()
    }
}

fun CustomTagWrapper.toD2DocTagWrapper(): D2DocTagWrapper? {
    return when (name.lowercase()) {
        "d2" -> D2(root = root)
        "example" -> Example(root = root)
        "parent" -> Parent(root = root)
        "page" -> Page(root = root)
        else -> null
    }
}

fun Text.hrefOrBody() = params["href"] ?: body
