package d2.dokka.storybook.renderer

import d2.dokka.storybook.D2StorybookPlugin
import d2.dokka.storybook.location.D2StorybookLocationProvider
import d2.dokka.storybook.model.page.D2StorybookContentPage
import d2.dokka.storybook.model.page.FileData
import d2.dokka.storybook.model.page.ModelPageNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.dokka.base.renderers.OutputWriter
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.RendererSpecificPage
import org.jetbrains.dokka.pages.RenderingStrategy
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.query
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.renderers.Renderer

open class D2StorybookRenderer(
    private val context: DokkaContext
): Renderer {
    protected open val outputWriter = context.plugin<D2StorybookPlugin>().querySingle { outputWriter }

    protected open lateinit var locationProvider: D2StorybookLocationProvider

    protected open val preprocessors = context.plugin<D2StorybookPlugin>().query { storybookPreprocessors }

    private val renderers = mutableMapOf(
        FileData.ROOT to MainPageContentRenderer(true),
        FileData.MAIN to MainPageContentRenderer(false),
        FileData.DESCRIPTION to DescriptionPageContentRenderer(context),
        FileData.VISUAL_JSON to VisualJsonPageContentRenderer(),
        FileData.VISUAL_KOTLIN to VisualKotlinPageContentRenderer(),
//        FileData.VISUAL_YAML to VisualYamlPageContentRenderer(),
        "Default" to MarkdownRenderer(context)
    )

    private fun PageNode.renderer(): D2ContentRenderer {
        return if(this is ModelPageNode) {
            renderers[this.fileData]!!
        } else {
            renderers["Default"]!!
        }
    }

    override fun render(root: RootPageNode) {
        val newRoot = preprocessors.fold(root) { acc, t -> t(acc) }

        locationProvider =
            context.plugin<D2StorybookPlugin>()
                .querySingle { locationProviderFactory }
                .getLocationProvider(newRoot) as D2StorybookLocationProvider

        runBlocking(Dispatchers.Default) {
            renderPages(newRoot)
        }
    }

    protected open suspend fun renderPages(root: PageNode) {
        coroutineScope {
            renderPage(root)

            root.children.forEach {
                launch { renderPages(it) }
            }
        }
    }

    protected open suspend fun renderPage(page: PageNode) {
        val path by lazy {
            locationProvider.resolve(page, skipExtension = true)
        }

        when (page) {
            is ModulePageNode -> Unit
            is D2StorybookContentPage -> outputWriter.write(page, path, page.renderer())
            is ContentPage -> outputWriter.write(page, path, page.renderer())
            is RendererSpecificPage -> when (val strategy = page.strategy) {
                is RenderingStrategy.Copy -> outputWriter.writeResources(strategy.from, path)
                is RenderingStrategy.Write -> outputWriter.write(path, strategy.text, "")
                is RenderingStrategy.Callback -> outputWriter.write(path, strategy.instructions(this, page), "")
                RenderingStrategy.DoNothing -> Unit
            }
            else -> throw AssertionError(
                "Page ${page.name} cannot be rendered by renderer as it is not renderer specific nor contains any content"
            )
        }
    }

    private suspend fun OutputWriter.write(page: ContentPage, path: String, renderer: D2ContentRenderer) {
        write(path, renderer.buildPage(page, locationProvider, renderer::buildPageContent), "")
    }
}

