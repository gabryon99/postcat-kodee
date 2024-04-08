package me.gabryon.kodeedelivery.levels

import godot.RandomNumberGenerator

enum class MailboxHorizontalPosition {
    LEFT, RIGHT;
}

enum class MailboxVerticalPosition {
    UP, DOWN;
}

interface LevelLogic {

    /**
     * Maximum running speed of the main character. The speed
     * value should always be negative.
     */
    val maximumCharacterSpeed: Double

    /**
     * How many points the character should reach to go next level.
     */
    val pointsToNextLevel: Int

    fun mailboxesPerFace(rng: RandomNumberGenerator): Int

    fun generateMailbox(rng: RandomNumberGenerator): Pair<MailboxHorizontalPosition, MailboxVerticalPosition>
}
