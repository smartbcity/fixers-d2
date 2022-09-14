package sample

import f2.dsl.fnc.F2Function


/**
 * Retrieve a boring thing
 * @D2 function
 * @parent [BoringInterface]
 */
typealias BoringGetQueryFunction = F2Function<InheritedBoringGetQuery, BoringGetQueryResult>

/**
 * @D2 query
 * @parent [BoringGetQueryFunction]
 */
interface BoringGetQuery {
    /**
     * name
     * @example "blblbl"
     */
    val name: String

    /**
     * desc
     * @example null
     */
    val description: String?
}

/**
 * @D2 inherit
 */
interface InheritedBoringGetQuery: BoringGetQuery {
    /**
     * This should not appear anywhere
     */
    val invisible: String
}

/**
 * @D2 event
 * @parent [BoringGetQueryFunction]
 */
interface BoringGetQueryResult {

    /**
     * retrieved stuff
     * @example {
     *   "name": "blblbl",
     *   "description": "blblblblbl bl lbbllb lblblblbl",
     *   "things": [[]]
     * }
     */
    val boringStuff: InheritedBoringInterface?
}
