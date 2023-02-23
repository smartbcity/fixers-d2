package d2.dokka.storybook.model.code

interface WithChildren: CodeElement {
    val children: Collection<CodeElement>
}
