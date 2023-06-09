package d2.dokka.storybook.model.doc.tag

import org.jetbrains.dokka.model.doc.DocTag

data class D2(override val root: DocTag) : WithTextBody {
	val type = body?.let(D2Type::get)
}

enum class D2Type(val id: String, val order: Int) {
	PAGE("page", order = 0),
	AUTOMATE("automate", order = 1),
	API("api", order = 3),
	SERVICE("service", order = 5),
	MODEL("model", order = 10),
	FUNCTION("function", order = 20),
	QUERY("query", order = 30),
	RESULT("result", order = 35),
	COMMAND("command", order = 40),
	EVENT("event", order = 45),
	SECTION("section", order = 50),
	HIDDEN("hidden", order = -1),
	INHERIT("inherit", order = -1);

	companion object {
		operator fun get(id: String) = values().find { it.id == id.lowercase() }
	}
}
