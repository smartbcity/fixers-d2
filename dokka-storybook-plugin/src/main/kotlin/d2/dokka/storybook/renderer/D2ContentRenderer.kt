package d2.dokka.storybook.renderer

import org.jetbrains.dokka.base.resolvers.local.LocationProvider
import org.jetbrains.dokka.pages.ContentPage

interface D2ContentRenderer {

    var d2LocationProvider: LocationProvider

    fun buildPage(page: ContentPage, locationProvider: LocationProvider, content: (StringBuilder, ContentPage) -> Unit): String {
        this.d2LocationProvider = locationProvider
        return buildString {
            content(this, page)
        }
    }

    fun buildPageContent(context: StringBuilder, page: ContentPage)
}