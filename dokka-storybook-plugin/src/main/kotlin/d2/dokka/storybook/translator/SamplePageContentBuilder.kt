package d2.dokka.storybook.translator

import d2.dokka.storybook.model.doc.Example
import d2.dokka.storybook.model.doc.RootDocumentable
import d2.dokka.storybook.model.doc.firstD2TagOfTypeOrNull
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode

abstract class SamplePageContentBuilder(
    protected val contentBuilder: PageContentBuilder
): D2StorybookPageContentBuilder {

    override fun contentFor(d: Documentable): ContentNode? {
        return when (d) {
            is DClasslike -> contentFor(d)
            is RootDocumentable -> contentFor(d)
            else -> null
        }
    }

    private fun contentFor(c: DClasslike): ContentNode {
        return contentBuilder.contentFor(c, kind = ContentKind.Properties)  {
            +contentForExamples(c)
        }
    }

    private fun contentFor(r: RootDocumentable): ContentNode {
        return contentBuilder.contentFor(r, kind = ContentKind.Sample) {
            codeBlock(r.pageDocumentation!!.example!!.body, "json")
        }
    }

    private fun contentForExamples(c: DClasslike): List<ContentGroup> {
        return c.properties.mapNotNull { property ->
            val exampleTag = property.documentation.firstD2TagOfTypeOrNull<Example>()

            exampleTag?.body?.let { exampleTagBody ->
                contentBuilder.contentFor(property, sourceSets = property.sourceSets, kind = ContentKind.Main) {
                    text(property.name)
                    text(exampleTagBody)
                }
            }
        }
    }
}