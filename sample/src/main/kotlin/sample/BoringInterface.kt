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