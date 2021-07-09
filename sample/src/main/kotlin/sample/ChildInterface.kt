package sample

import d2.dsl.annotation.D2
import d2.dsl.annotation.D2Type

/**
 * The child
 *
 * @parent [BoringInterface]
 */
@D2(D2Type.MODEL)
interface ChildInterface {

    /**
     * Its toy
     * @example "a cube"
     */
    val toy: String
}