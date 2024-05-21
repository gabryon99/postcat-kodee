package me.gabryon.kodeedelivery.levels

import godot.RandomNumberGenerator
import godot.extensions.godotStatic

/**
 * Represents the position of a mailbox in a level.
 *
 * @property hoz The horizontal position of the mailbox.
 * @property ver The vertical position of the mailbox.
 * @property distanceFromPrevious The distance between this mailbox and the previous one.
 */
data class MailboxPosition(
    val hoz: Horizontal,
    val ver: Vertical,
    val distanceFromPrevious: Double
) {
    enum class Horizontal { LEFT, RIGHT }
    enum class Vertical { TOP, BOTTOM }
}

/**
 * Switches the horizontal direction of the mailbox position.
 * E.g., if `this == LEFT` then the returned value is `RIGHT`.
 *
 * @return The new horizontal direction after switching.
 */
fun MailboxPosition.Horizontal.switchDirection() = when (this) {
    MailboxPosition.Horizontal.LEFT -> MailboxPosition.Horizontal.RIGHT
    MailboxPosition.Horizontal.RIGHT -> MailboxPosition.Horizontal.LEFT
}

/**
 * Switches the vertical direction of the mailbox position.
 * E.g., if `this == BOTTOM` then the returned value is `TOP`.
 *
 * @return The new vertical direction after switching.
 */
fun MailboxPosition.Vertical.switchDirection() = when (this) {
    MailboxPosition.Vertical.BOTTOM -> MailboxPosition.Vertical.TOP
    MailboxPosition.Vertical.TOP -> MailboxPosition.Vertical.BOTTOM
}

object LevelUtility {
    val rng by godotStatic {
        RandomNumberGenerator().apply { seed = "vogliobeneasallo97".hashCode().toLong() }
    }
}

/**
 * Represents the logic of a game level.
 */
sealed interface LevelLogic {

    /**
     * Maximum running speed of the main character. The speed value should always be negative.
     */
    val maximumCharacterSpeed: Double

    /**
     * How many points the character should reach to go next level.
     */
    val pointsToNextLevel: Int

    /**
     * Random number generator, defaulting backed by [LevelUtility.rng].
     */
    val rng: RandomNumberGenerator
        get() = LevelUtility.rng

    /**
     * Defines how to create new mailboxes in the current world.
     * Example, see [DebugLevel0] or, the following:
     * ```kt
     * override fun mailboxes(): Sequence<MailboxPosition> = sequence {
     *      yield(...)
     * }
     * ```
     */
    fun mailboxes(): Sequence<MailboxPosition>
}

/**
 * Yields a [MailboxPosition] with the specified horizontal position, vertical position, and distance from the previous mailbox.
 *
 * @param hoz The horizontal position of the mailbox.
 * @param ver The vertical position of the mailbox.
 * @param distanceFromPrevious The distance between this mailbox and the previous one.
 */
suspend inline fun SequenceScope<MailboxPosition>.yieldBox(
    hoz: MailboxPosition.Horizontal,
    ver: MailboxPosition.Vertical,
    distanceFromPrevious: Double
) {
    yield(MailboxPosition(hoz, ver, distanceFromPrevious))
}

/**
 * Generates a random alternative between two values based on the given probability.
 *
 * @param alt1 The first alternative value.
 * @param alt2 The second alternative value.
 * @param probability The probability of selecting the first alternative. Must be a number in the range [0.0, 1.0].
 * @return The randomly selected alternative.
 * @throws IllegalArgumentException if the probability is not in the range [0.0, 1.0].
 */
fun <T> LevelLogic.generateAlternativeRandomly(alt1: T, alt2: T, probability: Double): T {
    require(probability in 0.0..1.0) { "The probability must be a number in [0.0, 1.0]" }
    return if (rng.randf() <= probability) alt1 else alt2
}

/**
 * Generates a random vertical position for a mailbox based on the given probability of the left position.
 *
 * Example:
 * ```kt
 * // Generate the LEFT position with a probability of 20%
 * generateRandomHorizontalPosition(0.2)
 * // Generate the RIGHT position with a probability of 20% (that is, left at 80%)
 *  generateRandomHorizontalPosition(1.0 - 0.2)
 * ```
 *
 * @param leftProb The probability of generating a position on the left side. Must be a number in the range [0.0, 1.0].
 * @return The generated horizontal position, either LEFT or RIGHT.
 */
fun LevelLogic.randomHorizontalPosition(leftProb: Double): MailboxPosition.Horizontal =
    generateAlternativeRandomly(
        MailboxPosition.Horizontal.LEFT,
        MailboxPosition.Horizontal.RIGHT,
        leftProb
    )

/**
 * Generates a random vertical position for a mailbox based on the given probability of the top position.
 *
 * Example:
 * ```kt
 * // Generate the UP position with a probability of 20%
 * generateRandomHorizontalPosition(0.2)
 * // Generate the BOTTOM position with a probability of 20% (that is, TOP at 80%)
 *  generateRandomHorizontalPosition(1.0 - 0.2)
 * ```
 *
 * @param topProb The probability of generating a top position. Must be a number in the range [0.0, 1.0].
 * @return The generated vertical position, either TOP or BOTTOM.
 * @throws IllegalArgumentException if the probability is not in the range [0.0, 1.0].
 */
fun LevelLogic.randomVerticalPosition(topProb: Double): MailboxPosition.Vertical =
    generateAlternativeRandomly(
        MailboxPosition.Vertical.TOP,
        MailboxPosition.Vertical.BOTTOM,
        topProb
    )