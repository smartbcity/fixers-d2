package d2.dokka.storybook.location

import d2.dokka.storybook.model.doc.d2DocTagExtra
import d2.dokka.storybook.model.doc.tag.Parent
import d2.dokka.storybook.model.doc.title
import d2.dokka.storybook.model.doc.toRootDocumentable
import d2.dokka.storybook.model.page.D2StorybookPageNode
import d2.dokka.storybook.model.page.FileData
import org.jetbrains.dokka.base.resolvers.local.DokkaLocationProvider
import org.jetbrains.dokka.base.resolvers.local.LocationProviderFactory
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.toDisplaySourceSets
import org.jetbrains.dokka.pages.ClasslikePageNode
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.PackagePageNode
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import java.util.IdentityHashMap

class D2StorybookLocationProvider(
    pageGraphRoot: RootPageNode,
    dokkaContext: DokkaContext
): DokkaLocationProvider(pageGraphRoot, dokkaContext, "") {

    class Factory(private val context: DokkaContext) : LocationProviderFactory {
        override fun getLocationProvider(pageNode: RootPageNode) =
            D2StorybookLocationProvider(pageNode, context)
    }

    override val pathsIndex: Map<PageNode, List<String>> = IdentityHashMap<PageNode, List<String>>().apply {
        fun registerPath(page: PageNode, prefix: List<String>) {
            val newPrefix = when {
                page is RootPageNode && page.forceTopLevelName -> prefix + PAGE_WITH_CHILDREN_SUFFIX
                page is ModulePageNode -> prefix
                page is D2StorybookPageNode && page.fileData == FileData.ROOT -> prefix
                else -> prefix + page.pathName
            }
            put(page, newPrefix)
            page.children.forEach { registerPath(it, newPrefix) }

        }
        put(pageGraphRoot, emptyList())
        pageGraphRoot.children.forEach { registerPath(it, emptyList()) }
    }

    private val documentableIndex = HashMap<DRI, Documentable>().apply {
        fun registerDocumentable(page: PageNode) {
            if (page is ContentPage) {
                page.documentable
                    ?.takeIf { it.dri != DRI.topLevel }
                    ?.let { put(it.dri, it) }
            }
            page.children.forEach(::registerDocumentable)
        }
        registerDocumentable(pageGraphRoot)
    }

    private val parentMap = documentableIndex.mapValues { (_, documentable) ->
        val parentDri = documentable.d2DocTagExtra().firstTagOfTypeOrNull<Parent>()?.target
            ?: documentable.toRootDocumentable().dri
        documentableIndex[parentDri]
    }

    override fun resolve(dri: DRI, sourceSets: Set<DisplaySourceSet>, context: PageNode?): String? {
        return super.resolve(dri, sourceSets, context)
            ?: super.resolve(dri, dokkaContext.configuration.sourceSets.toDisplaySourceSets(), context)
    }

    override fun pathTo(node: PageNode, context: PageNode?): String {
        fun pathFor(page: PageNode) = pathsIndex[page] ?: throw AssertionError(
            "${page::class.simpleName}(${page.name}) does not belong to the current page graph so it is impossible to compute its path"
        )

        val nodePath = pathFor(node)
        val contextPath = context?.let { pathFor(it) }.orEmpty()

        val commonPathElements = nodePath.asSequence().zip(contextPath.asSequence())
            .takeWhile { (a, b) -> a == b }.count()

        val endOfPath = when {
            node is D2StorybookPageNode -> if (node.fileData == FileData.ROOT) listOf("${node.name}${node.fileData.extension}") else listOf(node.fileData.toString())
            node is ClasslikePageNode || node.children.isNotEmpty() -> listOf(PAGE_WITH_CHILDREN_SUFFIX)
            else -> emptyList()
        }

        return List(contextPath.size - commonPathElements) { ".." }
            .ifEmpty { listOf(".") }
            .plus(nodePath.drop(commonPathElements))
            .plus(endOfPath)
            .joinToString("/")
    }

    fun resolveAnchor(dri: DRI, context: PageNode): String? {
        if (context !is ContentPage) {
            return null
        }

        val targetDocumentable = documentableIndex[dri] ?: return null
        val targetRoot = targetDocumentable.firstAncestor()
        val contextRoot = context.documentable!!.firstAncestor()

        val pathBuilder = StringBuilder()
        if (targetRoot !== contextRoot) {
            pathBuilder.append("/docs/${targetRoot.sanitizedTitle}--page")
        }
        pathBuilder.append("#${targetDocumentable.sanitizedTitle}")

        return pathBuilder.toString()
    }

    private val PageNode.pathName: String
        get() = if (this is PackagePageNode) name else identifierToFilename(name)

    private fun Documentable.parent(): Documentable? {
        return parentMap[dri]
    }

    private fun Documentable.firstAncestor(): Documentable {
        return this.parent()?.firstAncestor() ?: this
    }

    private val Documentable.sanitizedTitle
        get() = title().lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), "-")

    companion object {
        private val reservedFilenames = setOf("index", "con", "aux", "lst", "prn", "nul", "eof", "inp", "out")

        // Taken from: https://stackoverflow.com/questions/1976007/what-characters-are-forbidden-in-windows-and-linux-directory-names
        private val reservedCharacters = setOf('|', '>', '<', '*', ':', '"', '?', '%')

        fun identifierToFilename(name: String): String {
            if (name.isEmpty()) return "--root--"
            return sanitizeFileName(name, reservedFilenames, reservedCharacters)
        }

        fun sanitizeFileName(name: String, reservedFileNames: Set<String>, reservedCharacters: Set<Char>): String {
            val lowercase = name.replace("[A-Z]".toRegex()) { matchResult -> "-" + matchResult.value.lowercase() }
                .removePrefix("-")
            val withoutReservedFileNames = if (lowercase in reservedFileNames) "--$lowercase--" else lowercase
            return reservedCharacters.fold(withoutReservedFileNames) { acc, character ->
                if (character in acc) acc.replace(character.toString(), "[${character.code}]")
                else acc
            }
        }
    }
}