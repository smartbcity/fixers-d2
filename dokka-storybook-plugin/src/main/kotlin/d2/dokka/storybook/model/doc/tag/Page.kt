package d2.dokka.storybook.model.doc.tag

import org.jetbrains.dokka.base.parsers.MarkdownParser
import org.jetbrains.dokka.model.doc.Br
import org.jetbrains.dokka.model.doc.CustomTagWrapper
import org.jetbrains.dokka.model.doc.Description
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.Li
import org.jetbrains.dokka.model.doc.Ol
import org.jetbrains.dokka.model.doc.P
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.doc.Ul
import org.jetbrains.dokka.model.withDescendants


data class Page(
    override val root: DocTag
): D2DocTagWrapper {
    val title: Title?
    val description: Description?
    val visual: Visual?

    init {
        fun DocTag.parse(): String {
            return when (this) {
                is DocumentationLink -> "[${children.first().parse()}]${href()}"
                is Text -> body.replace("@@", "\n@")
                is P -> "\n\n${children.joinToString("", transform = DocTag::parse)}"
                is Br -> "\\\n"
                is Ul -> children.joinToString("", postfix = "\n") { "\n- ${it.parse()}" }
                is Ol -> children.mapIndexed { i, docTag ->
                    val listIndex = i + (params["start"]?.toInt() ?: 1)
                    "\n$listIndex. ${docTag.parse()}"
                }.joinToString("", postfix = "\n")
                is Li -> children.joinToString("", transform = DocTag::parse).trim()
                else -> children.joinToString("" , transform = DocTag::parse)
            }
        }

        val childrenTags = root.parse()
        val docLinks = root.withDescendants()
            .filterIsInstance<DocumentationLink>()
            .associateBy { docLink -> docLink.href()?.removeSurrounding("[", "]") }

        val tags = MarkdownParser({ text -> docLinks[text]?.dri }, null)
            .parse(childrenTags)
            .children
            .map { child ->
                if (child is CustomTagWrapper) child.toD2DocTagWrapper() ?: child else child
            }

        title = tags.filterIsInstance<Title>().firstOrNull()
        description = tags.filterIsInstance<Description>().firstOrNull()
        visual = tags.filterIsInstance<Visual>().firstOrNull()
    }
}
