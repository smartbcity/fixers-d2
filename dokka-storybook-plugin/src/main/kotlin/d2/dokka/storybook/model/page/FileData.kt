package d2.dokka.storybook.model.page

import d2.dokka.storybook.model.render.D2ContentKind
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.Kind

interface WithFileData {
    val fileData: FileData
}

enum class FileData(
    val id: String,
    val extension: String,
    val language: CodeLanguage,
    val kind: Kind
) {
    ROOT("root", ".stories.mdx", CodeLanguage.REACT, ContentKind.Main),
    MAIN("index", ".mdx", CodeLanguage.REACT, ContentKind.Main),
    DESCRIPTION("desc", ".md", CodeLanguage.MARKDOWN, D2ContentKind.Description),
    DESCRIPTION_LEFT("descLeft", ".md", CodeLanguage.MARKDOWN, D2ContentKind.Description),
    DESCRIPTION_RIGHT("descRight", ".md", CodeLanguage.MARKDOWN, D2ContentKind.Description),
    VISUAL_JSON("visualJson", ".json", CodeLanguage.JSON, D2ContentKind.Visual),
    VISUAL_KOTLIN("visualKotlin", ".kt", CodeLanguage.KOTLIN, D2ContentKind.Visual),
    VISUAL_YAML("visualYaml", ".yml", CodeLanguage.YAML, D2ContentKind.Visual);

    override fun toString() = "$id$extension"

    companion object {
        fun fromId(id: String): FileData {
            return values().first { it.id == id }
        }
    }
}

enum class CodeLanguage(val id: String) {
    MARKDOWN("markdown"),
    JSON("json"),
    KOTLIN("kotlin"),
    REACT("react"),
    YAML("yaml")
}
