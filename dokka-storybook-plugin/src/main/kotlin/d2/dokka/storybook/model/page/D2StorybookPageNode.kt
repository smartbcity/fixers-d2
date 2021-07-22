package d2.dokka.storybook.model.page

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.PageNode

interface WithFileData {
    val fileData: FileData
}

enum class FileData(val id: String, val extension: String) {
    ROOT("root", ".stories.mdx"),
    MAIN("index", ".mdx"),
    DESCRIPTION("desc", ".md"),
    SAMPLE("sample", ".json");

    override fun toString() = "$id$extension"

    companion object {
        fun fromId(id: String): FileData {
            return values().first { it.id == id }
        }
    }
}

interface D2StorybookPageNode: PageNode, WithFileData

interface D2StorybookContentPage: D2StorybookPageNode, ContentPage {
    override fun modified(name: String, children: List<PageNode>): D2StorybookContentPage {
        return modified(name = name, content = this.content, dri = dri, children = children)
    }

    override fun modified(name: String, content: ContentNode, dri: Set<DRI>, embeddedResources: List<String>, children: List<PageNode>): D2StorybookContentPage

    fun isModified(name: String, content: ContentNode, embeddedResources: List<String>, children: List<PageNode>): Boolean {
        return name == this.name &&
                content === this.content &&
                embeddedResources === this.embeddedResources &&
                children shallowEq this.children
    }
}

internal infix fun <T> List<T>.shallowEq(other: List<T>) =
    this === other || (this.size == other.size && (this zip other).all { (a, b) -> a === b })