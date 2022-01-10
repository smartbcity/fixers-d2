package sample

/**
 * Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,
 * quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
 * Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
 * @D2 page
 * @title Sample/Boring Documentation
 * @visual json
 * {
 *   "name": "Stuff",
 *   "description": "Phrase describing the stuff",
 *   "things": [["Weird thingy", "Eww wat is dis"]]
 * }
 */
interface BoringInterfacePage

/**
 * @D2 section
 * @title Json section
 * @parent [BoringInterfacePage]
 * @order 100
 */
interface BoringInterfaceJsonSection

/**
 * @D2 section
 * @title Kotlin section
 * @parent [BoringInterfacePage]
 * @order 20
 */
interface BoringInterfaceKotlinSection

/**
 * This thing is so dull that no one knows how it could have ever been created
 *
 * @D2 model
 * @author unknown but they should be loathed for their creation
 * @title Boring Object
 * @parent [BoringInterfaceJsonSection]
 */
interface BoringInterface {
    /**
     * The name of smth not interesting at all
     * @example "Stuff"
     */
    val name: String

    /**
     * The most boring description you could ever think of
     * @example "Well calling it stuff is a bit... It's more like a pile of things being... existent"
     */
    val description: String

    /**
     * A vain attempt to make it look like there are a lot to see here
     * @example [["Weird thingy", "Eww wat is dis"]]
     */
    val things: List<String?>
}

/**
 * @D2 model
 * @title Boring Object but in Kotlin
 * @parent [BoringInterfaceKotlinSection]
 * @visual kotlin
 */
interface BoringInterfaceKotlin {
    /**
     * The name of smth not interesting at all
     * @example "Stuff"
     */
    val name: String

    /**
     * The most boring description you could ever think of
     * @example "Well calling it stuff is a bit... It's more like a pile of things being... existent"
     */
    val description: String

    /**
     * A vain attempt to make it look like there are a lot to see here
     * @example listOf("Weird thingy2", "Eww wat is dis")
     */
    val things: List<String?>
}

/**
 * @D2 model
 * @title Boring Object but in Kotlin
 * @parent [BoringInterfaceKotlinSection]
 * @visual kotlin BoringInterfaceKotlin2(
 *     name = "Stuff",
 *     description = "Ok",
 *     things = emptyList()
 * )
 */
interface BoringInterfaceKotlin2 {
    /**
     * The name of smth not interesting at all
     */
    val name: String

    /**
     * The most boring description you could ever think of
     */
    val description: String

    /**
     * A vain attempt to make it look like there are a lot to see here
     */
    val things: List<String?>
}
