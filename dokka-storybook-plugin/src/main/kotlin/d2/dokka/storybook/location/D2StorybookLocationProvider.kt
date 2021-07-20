package d2.dokka.storybook.location

import d2.dokka.storybook.model.page.D2StorybookPageNode
import org.jetbrains.dokka.base.resolvers.local.DokkaLocationProvider
import org.jetbrains.dokka.base.resolvers.local.LocationProviderFactory
import org.jetbrains.dokka.pages.ClasslikePageNode
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext

class D2StorybookLocationProvider(
    pageGraphRoot: RootPageNode,
    dokkaContext: DokkaContext
): DokkaLocationProvider(pageGraphRoot, dokkaContext, ".mdx") {

    class Factory(private val context: DokkaContext) : LocationProviderFactory {
        override fun getLocationProvider(pageNode: RootPageNode) =
            D2StorybookLocationProvider(pageNode, context)
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
            node is D2StorybookPageNode -> listOf(node.fileData.id)
            node is ClasslikePageNode || node.children.isNotEmpty() -> listOf(PAGE_WITH_CHILDREN_SUFFIX)
            else -> emptyList()
        }

        return List(contextPath.size - commonPathElements) { ".." }
            .plus(nodePath.drop(commonPathElements))
            .plus(endOfPath)
            .joinToString("/")
    }
}