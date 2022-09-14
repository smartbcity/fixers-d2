package d2.dokka.storybook.service

import d2.dokka.storybook.model.doc.PageDocumentable
import d2.dokka.storybook.model.doc.RootDocumentable
import d2.dokka.storybook.model.doc.SectionDocumentable
import d2.dokka.storybook.model.doc.tag.D2Type
import d2.dokka.storybook.model.doc.utils.d2Type
import d2.dokka.storybook.model.doc.utils.isOfType
import d2.dokka.storybook.model.doc.utils.visualType
import d2.dokka.storybook.model.page.FileData
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable

object DocumentablePageSelector {
    fun filesFor(d: Documentable): List<FileData> {
        if (d.isOfType(D2Type.INHERIT)) {
            return emptyList()
        }

        return when (d) {
            is RootDocumentable -> filesFor(d)
            is PageDocumentable -> filesFor(d)
            is SectionDocumentable -> filesFor(d)
            is DClasslike -> filesFor(d)
            is DTypeAlias -> filesFor(d)
            else -> emptyList()
        }
    }

    fun filesFor(d: RootDocumentable) = listOfNotNull(
        FileData.ROOT,
        FileData.MAIN,
        FileData.DESCRIPTION.takeIf { d.hasDescription },
        d.visualType().fileData
    )

    fun filesFor(d: PageDocumentable) = listOfNotNull(
        FileData.ROOT,
        FileData.MAIN,
        FileData.DESCRIPTION.takeIf { d.hasDescription },
        d.visualType().fileData
    )

    fun filesFor(d: SectionDocumentable) = listOfNotNull(
        FileData.MAIN,
        FileData.DESCRIPTION.takeIf { d.hasDescription },
        d.visualType().fileData
    )

    fun filesFor(d: DClasslike) = when (d.d2Type()) {
        D2Type.SERVICE -> listOfNotNull(
            FileData.MAIN,
            FileData.DESCRIPTION_LEFT,
            FileData.DESCRIPTION_RIGHT,
            d.visualType().fileData
        )
        else -> listOfNotNull(
            FileData.MAIN,
            FileData.DESCRIPTION,
            d.visualType().fileData
        )
    }

    fun filesFor(d: DTypeAlias) = listOfNotNull(
        FileData.MAIN,
        FileData.DESCRIPTION,
        d.visualType().fileData
    )
}
