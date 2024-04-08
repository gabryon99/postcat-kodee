package me.gabryon.kodeedelivery.levels

import godot.RandomNumberGenerator

class DebugLevel : LevelLogic {

    override val maximumCharacterSpeed: Double = -3.0
    override val pointsToNextLevel: Int = Int.MAX_VALUE

    override fun mailboxesPerFace(rng: RandomNumberGenerator): Int = 1

    override fun generateMailbox(rng: RandomNumberGenerator): Pair<MailboxHorizontalPosition, MailboxVerticalPosition> {
        val hPos = if (rng.randf() <= 0.5) MailboxHorizontalPosition.LEFT else MailboxHorizontalPosition.RIGHT
        return Pair(hPos, MailboxVerticalPosition.DOWN)
    }
}