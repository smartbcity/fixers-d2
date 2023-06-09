package d2.dokka.storybook.model.page

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.PageNode

class ModelPageNode(
	override val name: String,
	override val content: ContentNode,
	override val dri: Set<DRI>,
	override val documentables: List<Documentable>,
	override var children: List<PageNode>,
	override val embeddedResources: List<String> = emptyList(),
	override val fileData: FileData
): D2StorybookContentPage {
	override fun modified(name: String, children: List<PageNode>): ModelPageNode {
		return modified(name = name, content = content, dri = dri, children = children)
	}

	override fun modified(
		name: String, content: ContentNode, dri: Set<DRI>, embeddedResources: List<String>, children: List<PageNode>
	): ModelPageNode {
		if (!isModified(name, content, embeddedResources, children)) return this

		return ModelPageNode(
			name = name,
			content = content,
			dri = dri,
			documentables = documentables,
			children = children,
			embeddedResources = embeddedResources,
			fileData = fileData
		)
	}
}
