package me.gabryon.kodeedelivery.levels

import godot.RandomNumberGenerator

data class MailboxPosition(
    val hoz: HorizontalPosition,
    val ver: VerticalPosition,
    val distanceFromPrevious: Double
) {
    enum class HorizontalPosition { LEFT, RIGHT }
    enum class VerticalPosition { TOP, BOTTOM }
}

sealed interface LevelLogic {

    /**
     * Maximum running speed of the main character. The speed value should always be negative.
     */
    val maximumCharacterSpeed: Double

    /**
     * How many points the character should reach to go next level.
     */
    val pointsToNextLevel: Int

    val rng: RandomNumberGenerator

    fun mailboxes(): Sequence<MailboxPosition>
}

suspend inline fun SequenceScope<MailboxPosition>.yieldBox(
    hoz: MailboxPosition.HorizontalPosition,
    ver: MailboxPosition.VerticalPosition,
    distanceFromPrevious: Double
) {
    yield(MailboxPosition(hoz, ver, distanceFromPrevious))
}
