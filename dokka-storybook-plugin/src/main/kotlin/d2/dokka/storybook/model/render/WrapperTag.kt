package d2.dokka.storybook.model.render

enum class WrapperTag(private val id: String) {
    Article("article");

    fun open() = "<$id>"
    fun close() = "</$id>"
}