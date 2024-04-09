package me.gabryon.kodeedelivery.levels

import godot.RandomNumberGenerator

enum class MailboxHorizontalPosition {
    LEFT, RIGHT;
}

enum class MailboxVerticalPosition {
    TOP, BOTTOM;
}

data class MailboxPosition(val hoz: MailboxHorizontalPosition, val ver: MailboxVerticalPosition, val distanceFromPrevious: Double)


/**
 * Represents a slice in a level of a game.
 *
 * @param boxes An array of mailbox positions in the slice.
 * @param angularPos Where to place the slice.
 */
data class Slice(val boxes: Array<MailboxPosition>, val angularPos: Double)

interface LevelLogic {

    /**
     * Maximum running speed of the main character. The speed value should always be negative.
     */
    val maximumCharacterSpeed: Double

    /**
     * How many points the character should reach to go next level.
     */
    val pointsToNextLevel: Int

    fun mailboxes(): Sequence<MailboxPosition>
}

