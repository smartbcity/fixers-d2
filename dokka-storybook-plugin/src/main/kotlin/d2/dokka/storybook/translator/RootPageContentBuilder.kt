package d2.dokka.storybook.translator

import d2.dokka.storybook.model.doc.RootDocumentable
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode

internal abstract class RootPageContentBuilder(
    protected val contentBuilder: PageContentBuilder
): D2StorybookPageContentBuilder {

    override fun contentFor(d: Documentable): ContentNode? {
        return when (d) {
            is RootDocumentable -> contentFor(d)
            else -> null
        }
    }

    private fun contentFor(r: RootDocumentable): ContentNode {
        return contentBuilder.contentFor(
            dri = r.dri,
            sourceSets = r.sourceSets,
            kind = ContentKind.Extensions
        ) {}
    }
}