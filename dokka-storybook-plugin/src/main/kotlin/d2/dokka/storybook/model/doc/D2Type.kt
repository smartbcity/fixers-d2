package d2.dokka.storybook.model.doc

import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

enum class D2Type(val id: String, val order: Int) {
    MODEL("model", 10),
    FUNCTION("function", 20),
    COMMAND("command", 30),
    EVENT("event", 40);

    companion object {
        operator fun get(id: String) = values().find { it.id == id.toLowerCaseAsciiOnly() }
    }
}