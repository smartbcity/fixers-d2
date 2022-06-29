package d2.dokka.storybook.model.doc.tag

import d2.dokka.storybook.model.page.FileData
import org.jetbrains.dokka.model.doc.DocTag

sealed interface Visual: D2DocTagWrapper, WithOneParam {
    val type: VisualType
        get() = param?.let(VisualType::get) ?: VisualType.DEFAULT
}
data class VisualSimple(override val root: DocTag): Visual
data class VisualText(override val root: DocTag): Visual, WithOneParamAndTextBody
data class VisualLink(override val root: DocTag): Visual, WithTarget

enum class VisualType(val id: String, val fileData: FileData?) {
    NONE("none", null),
    JSON("json", FileData.VISUAL_JSON),
    KOTLIN("kotlin", FileData.VISUAL_KOTLIN),
    YAML("yaml", FileData.VISUAL_YAML);

    companion object {
        val DEFAULT = JSON
        operator fun get(id: String) = values().find { it.id == id.lowercase() }
    }
}
