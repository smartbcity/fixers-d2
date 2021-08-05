package sample

/**
 * I expected this. This is all within my calculations.
 * @d2 model
 * @parent [ExpectedInterface]
 */
interface ExpectedResult: ExpectedInterface {
    override val predictedStuff: String
}

/**
 * ... OK.
 * @d2 model
 * @parent [ExpectedResult]
 */
interface ReallyExpectedResult: ExpectedResult