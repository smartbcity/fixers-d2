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
import org.jetbrains.dokka.model.withDescendants
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

sealed interface D2DocTagWrapper: WithChildren<DocTag> {
    val root: DocTag
    override val children: List<DocTag>
        get() = root.children
}

sealed interface WithTextBody: D2DocTagWrapper {
    val body: String?
        get() = root.firstMemberOfType<P>()
            .childrenOfType<Text>() // text between [] is parsed as a reference and stored in a dedicated Text tag wrapper
            .joinToString("", transform = Text::hrefOrBody)

    fun isEmpty() = body == null
}

sealed interface WithTarget: D2DocTagWrapper {
    val target: DRI?
        get() = root.firstMemberOfTypeOrNull<DocumentationLink>()?.dri

    fun isEmpty() = target == null
}

data class D2(override val root: DocTag): WithTextBody

sealed interface Example: D2DocTagWrapper
data class ExampleText(override val root: DocTag): Example, WithTextBody
data class ExampleLink(override val root: DocTag): Example, WithTarget

data class Parent(override val root: DocTag): WithTarget
data class Title(override val root: DocTag): WithTextBody

data class Page(
    override val root: DocTag
): D2DocTagWrapper {
    val title: Title?
    val description: Description?
    val example: Example?

    init {
        fun DocTag.parse(): String {
            return when (this) {
                is DocumentationLink -> listOf(href())
                is Text -> hrefOrBody()
                    .replace("@@", "\n@")
                    .split("\n")
                    .filter(String::isNotBlank)
                else -> children.map(DocTag::parse)
            }.joinToString("\n")
        }

        val childrenTags = root.parse()
        val docLinks = root.withDescendants()
            .filterIsInstance<DocumentationLink>()
            .associateBy { docLink -> docLink.firstMemberOfTypeOrNull<Text>()?.body }

        val tags = MarkdownParser({ text -> docLinks[text]?.dri }, null)
            .parse(childrenTags)
            .children
            .map { child ->
                if (child is CustomTagWrapper) child.toD2DocTagWrapper() ?: child else child
            }

        title = tags.firstIsInstanceOrNull()
        description = tags.firstIsInstanceOrNull()
        example = tags.firstIsInstanceOrNull()
    }
}

fun CustomTagWrapper.toD2DocTagWrapper(): D2DocTagWrapper? {
    return when (name.lowercase()) {
        "d2" -> ::D2
        "example" -> when {
            root.firstMemberOfTypeOrNull<DocumentationLink>() != null -> ::ExampleLink
            else -> ::ExampleText
        }
        "parent" -> ::Parent
        "page" -> ::Page
        "title" -> ::Title
        else -> null
    }?.invoke(root)
}

fun Text.hrefOrBody() = params["href"] ?: body
fun DocumentationLink.href() = params["href"]
