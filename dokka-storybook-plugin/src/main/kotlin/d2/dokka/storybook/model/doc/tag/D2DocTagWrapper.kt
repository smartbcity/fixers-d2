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

sealed interface WithOneParam: D2DocTagWrapper {
    val param: String?
        get() = root.asPlainText()?.substringBefore(' ')
}

sealed interface WithOneParamAndTextBody: WithTextBody, WithOneParam {
    override val body: String?
        get() = super.body?.substringAfter(' ')
}

sealed interface WithTarget: D2DocTagWrapper {
    val target: DRI?
        get() = root.firstMemberOfTypeOrNull<DocumentationLink>()?.dri
}

fun CustomTagWrapper.toD2DocTagWrapper(): D2DocTagWrapper? {
    return when (name.lowercase()) {
        "child" -> ::Child
        "d2" -> ::D2
        "example" -> onExample()
        "order" -> ::Order
        "page" -> ::Page
        "parent" -> ::Parent
        "ref" -> ::Ref
        "title" -> ::Title
        "visual" -> onVisual()
        else -> null
    }?.invoke(root)
}

private fun CustomTagWrapper.onVisual() = when {
    root.hasDocumentationLink() -> ::VisualLink
    root.hasContentAfterFirstParameter() -> ::VisualText
    else -> ::VisualSimple
}

private fun CustomTagWrapper.onExample() = when {
    root.hasDocumentationLink() -> ::ExampleLink
    else -> ::ExampleText
}

fun DocTag.asPlainText() = firstMemberOfType<P>()
    .childrenOfType<Text>() // text between [] is parsed as a reference and stored in a dedicated Text tag wrapper
    .joinToString("", transform = Text::body)
    .trim()
    .ifBlank { null }

fun DocTag.hasDocumentationLink() = firstMemberOfTypeOrNull<DocumentationLink>() != null

fun DocTag.hasContentAfterFirstParameter() = !asPlainText()?.substringAfter(' ', "").isNullOrBlank()

fun DocumentationLink.href() = params["href"]
