package d2.dokka.storybook

import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.gfm.GfmPlugin
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.transformers.pages.PageTransformer

class D2StorybookPlugin: DokkaPlugin() {

    private val gfmPlugin by lazy { plugin<GfmPlugin>() }

    val storybookPreprocessors by extensionPoint<PageTransformer>()

    val renderer by extending {
        CoreExtensions.renderer providing ::D2StorybookRenderer override gfmPlugin.renderer
    }
}
