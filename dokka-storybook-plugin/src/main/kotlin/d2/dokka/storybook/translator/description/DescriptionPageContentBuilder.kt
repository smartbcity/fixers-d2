package d2.dokka.storybook.translator.description

import d2.dokka.storybook.model.doc.DocumentableIndexes
import d2.dokka.storybook.model.doc.RootDocumentable
import d2.dokka.storybook.model.doc.utils.isOfType
import d2.dokka.storybook.model.doc.tag.D2Type
import d2.dokka.storybook.model.doc.utils.title
import d2.dokka.storybook.translator.D2StorybookPageContentBuilder
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentNode

abstract class DescriptionPageContentBuilder: D2StorybookPageContentBuilder {
    protected abstract val documentableIndexes: DocumentableIndexes

    protected abstract fun contentForComments(d: Documentable): List<ContentNode>
    protected abstract fun contentForDescription(d: Documentable): List<ContentNode>

    protected fun PageContentBuilder.DocumentableContentBuilder.buildTitle(d: Documentable) {
        header(d.headerLevel(), d.title().substringAfterLast("/"))
    }

    protected fun Documentable.headerLevel(): Int {
        val parent = documentableIndexes.childToParentBiMap[dri]?.let(documentableIndexes.documentables::get)
            ?: return 1

        val increaseCount = (parent is RootDocumentable && parent.pageDocumentation != null) ||
                parent.isOfType(D2Type.FUNCTION, D2Type.SECTION, D2Type.PAGE)
        val headerInc = if (increaseCount) 1 else 0

        return parent.headerLevel() + headerInc
    }
}
