package d2.dokka.storybook.translator

import com.intellij.util.containers.BidirectionalMap
import d2.dokka.storybook.model.doc.D2Documentable
import d2.dokka.storybook.model.doc.PageDocumentable
import d2.dokka.storybook.model.doc.RootDocumentable
import d2.dokka.storybook.model.doc.SectionDocumentable
import d2.dokka.storybook.model.doc.d2Type
import d2.dokka.storybook.model.doc.visualType
import d2.dokka.storybook.model.doc.weight
import d2.dokka.storybook.model.page.FileData
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.links.sureClassNames
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import java.util.SortedSet

internal abstract class MainPageContentBuilder(
    private val contentBuilder: PageContentBuilder,
    private val documentables: Map<DRI, Documentable>,
    private val childToParentBiMap: BidirectionalMap<DRI, DRI>
): D2StorybookPageContentBuilder {

    override fun contentFor(d: Documentable): ContentNode? {
        return when (d) {
            is RootDocumentable -> contentFor(d)
            is PageDocumentable -> contentFor(d)
            is SectionDocumentable -> contentFor(d)
            is DClasslike -> contentFor(d)
            is DTypeAlias -> contentFor(d)
            else -> null
        }
    }

    private fun contentFor(c: DClasslike): ContentNode {
        return contentFor(c)  {
            group(setOf(c.dri), kind = ContentKind.Source) {
                text(FileData.DESCRIPTION.id, kind = ContentKind.Comment)
                c.visualType()?.fileData?.let { fileData ->
                    text(fileData.id, kind = ContentKind.Sample)
                }
            }
        }
    }

    private fun contentFor(t: DTypeAlias): ContentNode {
        return contentFor(t) {
            group(setOf(t.dri), kind = ContentKind.Source) {
                text(FileData.DESCRIPTION.id, kind = ContentKind.Comment)
                t.visualType()?.fileData?.let { fileData ->
                    text(fileData.id, kind = ContentKind.Sample)
                }
            }
        }
    }

    private fun contentFor(r: RootDocumentable): ContentNode {
        return contentFor(r) {
            group(setOf(r.dri), kind = ContentKind.Source) {
                if (r.hasDescription) {
                    text(FileData.DESCRIPTION.id, kind = ContentKind.Comment)
                }
                if (r.hasVisual) {
                    r.pageDocumentation?.visual?.type?.fileData?.let { fileData ->
                        text(fileData.id, kind = ContentKind.Sample)
                    }
                }
            }
        }
    }

    private fun contentFor(d: D2Documentable): ContentNode {
        return contentFor(d) {
            group(setOf(d.dri), kind = ContentKind.Source) {
                if (d.hasDescription) {
                    text(FileData.DESCRIPTION.id, kind = ContentKind.Comment)
                }
                d.visualType()?.fileData?.let { fileData ->
                    text(fileData.id, kind = ContentKind.Sample)
                }
            }
        }
    }

    private fun contentFor(d: Documentable, block: PageContentBuilder.DocumentableContentBuilder.() -> Unit = {}): ContentGroup {
        return contentBuilder.contentFor(d, kind = ContentKind.Main)  {
            block()
            +contentForChildrenOf(d)
        }
    }

    private fun contentForChildrenOf(d: Documentable): ContentNode {
        return contentBuilder.contentFor(
            dri = childToParentBiMap.getKeysByValue(d.dri).orEmpty().mapNotNull(documentables::get).driSortedByD2Type(),
            sourceSets = d.sourceSets,
            kind = ContentKind.Extensions
        ) {}
    }

    private fun List<Documentable>.driSortedByD2Type(): SortedSet<DRI> {
        val typeMap = this.associate { d -> d.dri to d.d2Type() }
        val weightMap = this.associate { d -> d.dri to (d.weight() ?: Int.MAX_VALUE) }
        return this.map(Documentable::dri)
            .toSortedSet { dri1, dri2 ->
                (typeMap[dri1]?.order ?: 0 - (typeMap[dri2]?.order ?: 0)).takeIf { it != 0 }
                    ?: (weightMap[dri1] ?: 0 - (weightMap[dri2] ?: 0)).takeIf { it != 0 }
                    ?: dri1.sureClassNames.compareTo(dri2.sureClassNames, true)
            }
    }
}