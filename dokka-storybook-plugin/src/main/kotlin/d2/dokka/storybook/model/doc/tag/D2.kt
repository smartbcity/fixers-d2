package d2.dokka.storybook.model.doc.tag

import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

data class D2(override val root: DocTag): WithTextBody {
    val type = body?.let(D2Type::get)
}

enum class D2Type(val id: String, val order: Int) {
    PAGE("page", 0),
    SECTION("section", 0),
    MODEL("model", 10),
    FUNCTION("function", 20),
    COMMAND("command", 30),
    QUERY("query", 30),
    EVENT("event", 40);

    companion object {
        operator fun get(id: String) = values().find { it.id == id.toLowerCaseAsciiOnly() }
    }
}