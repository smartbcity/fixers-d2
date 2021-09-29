package d2.dokka.storybook.model.doc

import d2.dokka.storybook.model.doc.tag.Page
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.SourceSetDependent
import org.jetbrains.dokka.model.doc.DocumentationNode
import org.jetbrains.dokka.model.properties.PropertyContainer
import org.jetbrains.dokka.model.properties.WithExtraProperties

data class RootDocumentable(
    override val name: String,
    override val dri: DRI,
    override val documentation: SourceSetDependent<DocumentationNode>,
    override val expectPresentInSet: DokkaConfiguration.DokkaSourceSet?,
    override val sourceSets: Set<DokkaConfiguration.DokkaSourceSet>,
    override val children: List<Documentable>,
    override val extra: PropertyContainer<Documentable>
): Documentable(), WithExtraProperties<Documentable> {
    val pageDocumentation: Page? = extra[D2DocTagExtra]?.firstTagOfTypeOrNull()
    val hasDescription = pageDocumentation?.description != null
    val hasExample = pageDocumentation?.example != null

    override fun withNewExtras(newExtras: PropertyContainer<Documentable>) = copy(extra = newExtras)
}
