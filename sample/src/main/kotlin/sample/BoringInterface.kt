package sample

import d2.dsl.annotation.D2
import d2.dsl.annotation.D2Type

/**
 * This thing is so dull that no one knows how it could have ever been created
 *
 * @author unknown but they should be loathed for their creation
 */
@D2(D2Type.MODEL)
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
     * @example \["Weird thingy", "Eww wat is dis"]
     */
    val things: List<String?>
}
