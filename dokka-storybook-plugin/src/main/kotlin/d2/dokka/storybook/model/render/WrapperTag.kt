package d2.dokka.storybook.model.render

sealed class WrapperTag(
    protected val name: String
) {

    open fun open() = "<$name>"
    open fun close() = "</$name>"
}

object Article: WrapperTag("article")
