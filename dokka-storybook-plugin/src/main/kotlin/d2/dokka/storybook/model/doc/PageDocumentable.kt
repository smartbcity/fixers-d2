package d2.dokka.storybook.model.doc

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.SourceSetDependent
import org.jetbrains.dokka.model.doc.DocumentationNode
import org.jetbrains.dokka.model.properties.PropertyContainer

data class PageDocumentable(
    override val children: List<Documentable>,
    override val documentation: SourceSetDependent<DocumentationNode>,
    override val dri: DRI,
    override val expectPresentInSet: DokkaConfiguration.DokkaSourceSet?,
    override val name: String?,
    override val sourceSets: Set<DokkaConfiguration.DokkaSourceSet>,
    override val extra: PropertyContainer<Documentable>
): D2Documentable() {
    override fun withNewExtras(newExtras: PropertyContainer<Documentable>) = copy(extra = newExtras)
}
