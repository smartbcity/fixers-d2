package d2.dokka.storybook.translator.root

import d2.dokka.storybook.model.doc.D2Documentable
import d2.dokka.storybook.model.doc.DocumentableIndexes
import d2.dokka.storybook.model.doc.PageDocumentable
import d2.dokka.storybook.model.doc.RootDocumentable
import d2.dokka.storybook.model.doc.SectionDocumentable
import d2.dokka.storybook.model.doc.d2Type
import d2.dokka.storybook.model.doc.isOfType
import d2.dokka.storybook.model.doc.tag.D2Type
import d2.dokka.storybook.model.doc.visualType
import d2.dokka.storybook.model.doc.weight
import d2.dokka.storybook.model.page.FileData
import d2.dokka.storybook.model.render.D2ContentKind
import d2.dokka.storybook.model.render.D2Marker
import d2.dokka.storybook.translator.D2StorybookPageContentBuilder
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.links.sureClassNames
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentNode
import java.util.SortedSet

internal abstract class MainPageContentBuilder(
    private val contentBuilder: PageContentBuilder,
    private val documentableIndexes: DocumentableIndexes,
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
            group(setOf(c.dri), kind = D2ContentKind.Source) {
                descriptionFile()
                c.visualType()?.fileData?.let { fileData ->
                    visualFile(fileData)
                }
            }
        }
    }

    private fun contentFor(t: DTypeAlias): ContentNode {
        return contentFor(t) {
            group(setOf(t.dri), kind = D2ContentKind.Source) {
                descriptionFile()
                t.visualType()?.fileData?.let { fileData ->
                    visualFile(fileData)
                }
            }
        }
    }

    private fun contentFor(r: RootDocumentable): ContentNode {
        return contentFor(r) {
            group(setOf(r.dri), kind = D2ContentKind.Source) {
                if (r.hasDescription) {
                    descriptionFile()
                }
                if (r.hasVisual) {
                    r.pageDocumentation?.visual?.type?.fileData?.let { fileData ->
                        visualFile(fileData)
                    }
                }
            }
        }
    }

    private fun contentFor(d: D2Documentable): ContentNode {
        return contentFor(d) {
            group(setOf(d.dri), kind = D2ContentKind.Source) {
                if (d.hasDescription) {
                    descriptionFile()
                }
                d.visualType()?.fileData?.let { fileData ->
                    visualFile(fileData)
                }
            }
        }
    }

    private fun contentFor(d: Documentable, block: PageContentBuilder.DocumentableContentBuilder.() -> Unit = {}): ContentGroup {
        return contentBuilder.contentFor(d, kind = D2ContentKind.Container)  {
            block()
            if (d.isOfType(D2Type.MODEL, D2Type.SERVICE)) {
                divider()
            }
            +contentForChildrenOf(d)
            if (d.isOfType(D2Type.FUNCTION)) {
                divider()
            }
        }
    }

    private fun contentForChildrenOf(d: Documentable): ContentNode {
        return contentBuilder.contentFor(
            dri = documentableIndexes.childToParentBiMap
                .getKeysByValue(d.dri)
                .orEmpty()
                .mapNotNull(documentableIndexes.documentables::get)
                .driSortedByD2Type(),
            sourceSets = d.sourceSets,
            kind = D2ContentKind.Children
        ) {}
    }

    private fun PageContentBuilder.DocumentableContentBuilder.descriptionFile() {
        text(FileData.DESCRIPTION.id, kind = D2ContentKind.Description)
    }

    private fun PageContentBuilder.DocumentableContentBuilder.visualFile(fileData: FileData) {
        text(fileData.id, kind = D2ContentKind.Visual)
    }

    private fun PageContentBuilder.DocumentableContentBuilder.divider() {
        text("", kind = D2Marker.Divider)
    }

    private fun List<Documentable>.driSortedByD2Type(): SortedSet<DRI> {
        val typeMap = this.associate { d -> d.dri to d.d2Type() }
        val weightMap = this.associate { d -> d.dri to (d.weight() ?: Int.MAX_VALUE) }
        return this.map(Documentable::dri)
            .toSortedSet { dri1, dri2 ->
                compareWeights(typeMap[dri1]?.order, typeMap[dri2]?.order).takeIf { it != 0 }
                    ?: compareWeights(weightMap[dri1], weightMap[dri2]).takeIf { it != 0 }
                    ?: dri1.sureClassNames.compareTo(dri2.sureClassNames, true)
            }
    }

    private fun compareWeights(w1: Int?, w2: Int?) = (w1 ?: Int.MAX_VALUE).compareTo(w2 ?: Int.MAX_VALUE)
}
