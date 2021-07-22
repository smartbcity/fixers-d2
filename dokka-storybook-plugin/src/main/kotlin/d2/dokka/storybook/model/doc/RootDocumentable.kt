package d2.dokka.storybook.model.doc

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.SourceSetDependent
import org.jetbrains.dokka.model.doc.DocumentationNode

class RootDocumentable(
    override val name: String,
    override val dri: DRI,
    override val documentation: SourceSetDependent<DocumentationNode>,
    override val expectPresentInSet: DokkaConfiguration.DokkaSourceSet?,
    override val sourceSets: Set<DokkaConfiguration.DokkaSourceSet>,
    override val children: List<Documentable>
): Documentable() {
    val pageDocumentation: Page? = documentation.firstD2TagOfTypeOrNull()
    val hasDescription = pageDocumentation?.description != null
    val hasExample = pageDocumentation?.example != null
}
