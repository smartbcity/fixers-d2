package d2.dokka.storybook.translator

import d2.dokka.storybook.model.doc.d2Type
import d2.dokka.storybook.model.page.FileData
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DInterface
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import java.util.SortedSet

abstract class D2StorybookMainPageCreator(
    protected val contentBuilder: PageContentBuilder,
    protected val childrenMap: Map<DRI, List<Documentable>>
) {

    fun contentFor(c: DClasslike): ContentNode {
        if (c is DInterface) {
            return mainContentFor(c)  {
                group(setOf(c.dri), kind = ContentKind.Source) {
                    text(FileData.DESCRIPTION.id, kind = ContentKind.Comment)
                    text(FileData.SAMPLE.id, kind = ContentKind.Sample)
                }
            }
        }

        return contentBuilder.contentFor(c, kind = ContentKind.Empty)
    }

    fun contentFor(t: DTypeAlias): ContentNode {
        return mainContentFor(t) {
            group(setOf(t.dri), kind = ContentKind.Source) {
                text(FileData.DESCRIPTION.id, kind = ContentKind.Comment)
            }
        }
    }

    private fun mainContentFor(d: Documentable, block: PageContentBuilder.DocumentableContentBuilder.() -> Unit = {}): ContentGroup {
        return contentBuilder.contentFor(d, kind = ContentKind.Main)  {
            block()
            +contentForChildrenOf(d)
        }
    }

    private fun contentForChildrenOf(d: Documentable): ContentNode {
        return contentBuilder.contentFor(
            dri = childrenMap[d.dri].orEmpty().driSortedByD2Type(),
            sourceSets = d.sourceSets,
            kind = ContentKind.Extensions
        ) {}
    }

    private fun List<Documentable>.driSortedByD2Type(): SortedSet<DRI> {
        val typeMap = this.associate { d -> d.dri to d.d2Type }

        return this.map(Documentable::dri)
            .toSortedSet { dri1, dri2 ->
                val doc1Type = typeMap[dri1]!!
                val doc2Type = typeMap[dri2]!!
                (doc1Type.order - doc2Type.order)
                    .takeIf { it != 0 }
                    ?: dri1.classNames!!.compareTo(dri2.classNames ?: "", true)
            }
    }
}