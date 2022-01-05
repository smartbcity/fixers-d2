package d2.dokka.storybook.translator

import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentNode

internal interface D2StorybookPageContentBuilder {
    fun contentFor(d: Documentable): ContentNode?
}
