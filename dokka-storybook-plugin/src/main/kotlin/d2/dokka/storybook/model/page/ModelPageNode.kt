package d2.dokka.storybook.model.page

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.PageNode

class ModelPageNode(
    override val name: String,
    override val content: ContentNode,
    override val dri: Set<DRI>,
    override val documentable: Documentable?,
    override val children: List<PageNode>,
    override val embeddedResources: List<String> = emptyList(),
    override val fileData: FileData
): D2StorybookContentPage {

    override fun modified(name: String, content: ContentNode, dri: Set<DRI>, embeddedResources: List<String>, children: List<PageNode>): ContentPage {
        if (isModified(name, content, embeddedResources, children)) return this

        return ModelPageNode(
            name = name,
            content = content,
            dri = dri,
            documentable = documentable,
            children = children,
            embeddedResources = embeddedResources,
            fileData = fileData
        )
    }
}