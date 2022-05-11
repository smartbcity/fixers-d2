package d2.dokka.storybook.model.doc.tag

import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

data class D2(override val root: DocTag) : WithTextBody {
	val type = body?.let(D2Type::get)
}

enum class D2Type(val id: String, val order: Int) {
	PAGE("page", order = 0),
	SECTION("section", order = 0),
	MODEL("model", order = 10),
	FUNCTION("function", order = 20),
	COMMAND("command", order = 30),
	QUERY("query", order = 30),
	EVENT("event", order = 40),
	SERVICE("service", order = 50);

	companion object {
		operator fun get(id: String) = values().find { it.id == id.toLowerCaseAsciiOnly() }
	}
}
