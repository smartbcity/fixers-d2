package sample

import f2.dsl.fnc.F2Function


/**
 * Retrieve a boring thing
 * @D2 function
 * @parent [BoringInterface]
 */
typealias BoringGetQueryFunction = F2Function<BoringGetQuery, BoringGetQueryResult>

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
    val boringStuff: BoringInterface?
}
