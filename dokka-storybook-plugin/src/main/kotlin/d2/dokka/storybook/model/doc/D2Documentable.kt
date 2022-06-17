package d2.dokka.storybook.model.doc

import d2.dokka.storybook.model.doc.utils.docTagWrappers
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.doc.Description
import org.jetbrains.dokka.model.properties.WithExtraProperties
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

abstract class D2Documentable: Documentable(), WithExtraProperties<Documentable> {
    val hasDescription by lazy { documentation.docTagWrappers().first.firstIsInstanceOrNull<Description>() != null }
}
