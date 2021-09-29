package d2.dokka.storybook.model.doc

import d2.dokka.storybook.model.doc.tag.D2DocTagWrapper
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.properties.ExtraProperty

data class D2DocTagExtra(val docTagWrappers: List<D2DocTagWrapper>): ExtraProperty<Documentable> {
    companion object: ExtraProperty.Key<Documentable, D2DocTagExtra>
    override val key: ExtraProperty.Key<Documentable, D2DocTagExtra> = D2DocTagExtra

    inline fun <reified T: D2DocTagWrapper> filterTagsOfType(): List<T> {
        return docTagWrappers.filterIsInstance<T>()
    }

    inline fun <reified T: D2DocTagWrapper> firstTagOfType(): T {
        return filterTagsOfType<T>().first()
    }

    inline fun <reified T: D2DocTagWrapper> firstTagOfTypeOrNull(): T? {
        return filterTagsOfType<T>().firstOrNull()
    }
}