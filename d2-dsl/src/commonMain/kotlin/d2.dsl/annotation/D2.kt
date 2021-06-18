package d2.dsl.annotation

@Target(AnnotationTarget.CLASS)
annotation class D2(
    val type: D2Type
)

enum class D2Type {
    MODEL, COMMAND, QUERY, EVENT
}
