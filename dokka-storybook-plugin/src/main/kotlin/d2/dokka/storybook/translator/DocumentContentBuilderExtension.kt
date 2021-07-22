package d2.dokka.storybook.translator

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.base.resolvers.anchors.SymbolAnchorHint
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.properties.PropertyContainer
import org.jetbrains.dokka.model.properties.plus
import org.jetbrains.dokka.model.toDisplaySourceSets
import org.jetbrains.dokka.pages.ContentCodeBlock
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentTable
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.pages.DCI
import org.jetbrains.dokka.pages.Kind
import org.jetbrains.dokka.pages.Style

fun <T: Documentable> PageContentBuilder.DocumentableContentBuilder.block(
    kind: Kind = ContentKind.Main,
    elements: Iterable<T>,
    sourceSets: Set<DokkaConfiguration.DokkaSourceSet> = mainSourcesetData,
    styles: Set<Style> = mainStyles,
    extra: PropertyContainer<ContentNode> = mainExtra,
    renderWhenEmpty: Boolean = false,
    needsSorting: Boolean = true,
    headers: List<ContentGroup> = emptyList(),
    needsAnchors: Boolean = false,
    operation: PageContentBuilder.DocumentableContentBuilder.(T) -> Unit
) {
    if (renderWhenEmpty || elements.any()) {
        +ContentTable(
            header = headers,
            children = elements
                .let {
                    if (needsSorting)
                        it.sortedWith(compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { it.name })
                    else it
                }
                .map {
                    val newExtra = if (needsAnchors) extra + SymbolAnchorHint.from(it, kind) else extra
                    buildGroup(setOf(it.dri), it.sourceSets.toSet(), kind, styles, newExtra) {
                        operation(it)
                    }
                },
            dci = DCI(mainDRI, kind),
            sourceSets = sourceSets.toDisplaySourceSets(),
            style = styles,
            extra = extra
        )
    }
}

fun PageContentBuilder.DocumentableContentBuilder.codeBlock(
    code: String,
    language: String,
    kind: ContentKind = ContentKind.Main,
    sourceSets: Set<DokkaConfiguration.DokkaSourceSet> = mainSourcesetData,
    styles: Set<Style> = mainStyles,
    extra: PropertyContainer<ContentNode> = mainExtra,
) {
    val dci = DCI(mainDRI, kind)
    val displaySourceSets = sourceSets.toDisplaySourceSets()
    +ContentCodeBlock(
        language = language,
        children = listOf(ContentText(code, dci = dci, sourceSets = displaySourceSets)),
        dci = dci,
        sourceSets = displaySourceSets,
        style = styles,
        extra = extra
    )
}