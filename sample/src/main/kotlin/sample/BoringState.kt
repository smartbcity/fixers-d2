package sample

/**
 * The state of a... well... huh never mind.
 * @d2 model
 * @visual automate sample/src/main/resources/activity.json
 */
enum class BoringState {
    /** Not doing anything. */
    IDLE,
    /** Should be doing something but is somehow not doing it. */
    PROCRASTINATING,
    /** Is mentally preparing for doing the thing. */
    MENTALLY_PREPARING,
    /** About to start the thing. */
    ABOUT_TO_GET_STARTED,
    /** Just a bit more before starting the thing. */
    ALMOST_STARTED,
    /** Has started to eventually start the thing. */
    STARTING_TO_START,
    /** Trying to figure out how to start the thing. */
    CONFUSED_START,
    /** Got distracted by something else. */
    DISTRACTED,
    /** Starting to doubt if the thing is worth doing. */
    DOUBTING,
    /** Gave up on the thing. It wasn't that important anyway. */
    GAVE_UP
}
