package d2.dokka.storybook.renderer

import d2.dokka.storybook.location.D2StorybookLocationProvider
import org.jetbrains.dokka.pages.ContentPage

interface D2ContentRenderer {

    var d2LocationProvider: D2StorybookLocationProvider

    fun buildPage(page: ContentPage, locationProvider: D2StorybookLocationProvider, content: (StringBuilder, ContentPage) -> Unit): String {
        this.d2LocationProvider = locationProvider
        return buildString {
            content(this, page)
        }
    }

    fun buildPageContent(context: StringBuilder, page: ContentPage)
}