package me.gabryon.kodeedelivery.levels

import me.gabryon.kodeedelivery.utility.increasedByFactorOf
import me.gabryon.kodeedelivery.utility.infiniteSequence

/**
 * Notes on Kodee's Speed
 * * At the game's beginning, Kodee's speed must be set at minimum 0.3
 */

data object DebugLevel0 : LevelLogic {

    override val maximumCharacterSpeed: Double = 0.3
    override val pointsToNextLevel: Int = 500

    /**
     * Generate mailboxes always on the left, distant 1.0 rad between each other.
     */
    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {
        yieldBox(
            hoz = MailboxPosition.Horizontal.LEFT,
            ver = MailboxPosition.Vertical.BOTTOM,
            distanceFromPrevious = 1.0,
        )
        yieldBox(
            hoz = MailboxPosition.Horizontal.RIGHT,
            ver = MailboxPosition.Vertical.TOP,
            distanceFromPrevious = 0.0,
        )
    }
}

data object DebugLevel1 : LevelLogic {

    override val maximumCharacterSpeed: Double = 2.0 increasedByFactorOf 10.0
    override val pointsToNextLevel: Int = -1

    /**
     * Generate a pair of mailboxes, distant 1.0 rad between each other.
     */
    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {
        yieldBox(
            hoz = MailboxPosition.Horizontal.LEFT,
            ver = MailboxPosition.Vertical.BOTTOM,
            distanceFromPrevious = 1.0,
        )
        yieldBox(
            hoz = MailboxPosition.Horizontal.RIGHT,
            ver = MailboxPosition.Vertical.BOTTOM,
            distanceFromPrevious = 0.0,
        )
    }
}
