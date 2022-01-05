package d2.dokka.storybook.model.code

interface WithParams: CodeElement {
    val params: Map<String, CodeElement>
}
