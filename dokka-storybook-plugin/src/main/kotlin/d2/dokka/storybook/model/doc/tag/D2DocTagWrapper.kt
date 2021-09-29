package d2.dokka.storybook.model.doc.tag

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.WithChildren
import org.jetbrains.dokka.model.childrenOfType
import org.jetbrains.dokka.model.doc.CustomTagWrapper
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.P
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.firstMemberOfType
import org.jetbrains.dokka.model.firstMemberOfTypeOrNull

sealed interface D2DocTagWrapper: WithChildren<DocTag> {
    val root: DocTag
    override val children: List<DocTag>
        get() = root.children
}

sealed interface WithTextBody: D2DocTagWrapper {
    val body: String?
        get() = root.asPlainText()
}

sealed interface WithTarget: D2DocTagWrapper {
    val target: DRI?
        get() = root.firstMemberOfTypeOrNull<DocumentationLink>()?.dri
}

fun CustomTagWrapper.toD2DocTagWrapper(): D2DocTagWrapper? {
    return when (name.lowercase()) {
        "child" -> ::Child
        "d2" -> ::D2
        "example" -> when {
            root.hasDocumentationLink() -> ::ExampleLink
            else -> ::ExampleText
        }
        "order" -> ::Order
        "page" -> ::Page
        "parent" -> ::Parent
        "title" -> ::Title
        else -> null
    }?.invoke(root)
}

fun DocTag.asPlainText() = firstMemberOfType<P>()
    .childrenOfType<Text>() // text between [] is parsed as a reference and stored in a dedicated Text tag wrapper
    .joinToString("", transform = Text::hrefOrBody)
    .trim()
    .ifBlank { null }

fun DocTag.hasDocumentationLink() = firstMemberOfTypeOrNull<DocumentationLink>() != null

fun Text.hrefOrBody() = params["href"] ?: body
fun DocumentationLink.href() = params["href"]