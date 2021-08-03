package sample

/**
 * The child
 * @D2 model
 * @parent [BoringInterface]
 * @order 10
 */
interface ChildInterface {

    /**
     * Its toy
     * @example "a cube"
     */
    val toy: String

    /**
     * The same things as you'd find in a BoringInterface
     * @example [BoringInterface.things]
     */
    val things: List<String?>
}