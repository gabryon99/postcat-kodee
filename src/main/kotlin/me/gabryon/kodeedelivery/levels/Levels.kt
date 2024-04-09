package me.gabryon.kodeedelivery.levels

import godot.RandomNumberGenerator
import me.gabryon.kodeedelivery.utility.infiniteSequence

data object DebugLevel0 : LevelLogic {

    override val maximumCharacterSpeed: Double = -3.0
    override val pointsToNextLevel: Int = Int.MAX_VALUE

    override val rng = RandomNumberGenerator().apply { seed = "kodee".hashCode().toLong() }

    /**
     * Generate mailboxes always on the left, distant 1.0 rad between each other.
     */
    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {
        yieldBox(
            hoz = MailboxPosition.HorizontalPosition.LEFT,
            ver = MailboxPosition.VerticalPosition.BOTTOM,
            distanceFromPrevious = 1.0,
        )
    }
}

data object DebugLevel1 : LevelLogic {

    override val maximumCharacterSpeed: Double = -5.0
    override val pointsToNextLevel: Int = 500

    override val rng = RandomNumberGenerator().apply { seed = "kodee".hashCode().toLong() }

    /**
     * Generate a pair of mailboxes, distant 1.0 rad between each other.
     */
    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {
        yieldBox(
            hoz = MailboxPosition.HorizontalPosition.LEFT,
            ver = MailboxPosition.VerticalPosition.BOTTOM,
            distanceFromPrevious = 1.0,
        )
        yieldBox(
            hoz = MailboxPosition.HorizontalPosition.RIGHT,
            ver = MailboxPosition.VerticalPosition.BOTTOM,
            distanceFromPrevious = 0.0,
        )
    }
}

/**
 * Level 1:
 * - At most one tower per face (it can also have 0).
 * - The probability that a tall tower appears is very low (2 out of 10 towers will be tall).
 * - Assuming that the current tower has appeared to the left, it is VERY LIKELY (about a 75% probability)
 *  that the next to be generated will be on the same side. This is to teach the player to create combos in an
 *  environment that encourages them to do so. This probability holds up to 3 towers, if the third tower is
 *  generated on the same side as the previous two, then the next tower has a 25% probability of being
 *  generated on the same side.
 * - TODO: Speed parameters need to be determined
 * - TODO: The speed is the base speed present in the tutorial.
 */
data object Level1 : LevelLogic {
    override val maximumCharacterSpeed: Double
        get() = -3.0
    override val pointsToNextLevel: Int
        get() = 2500

    override val rng = RandomNumberGenerator().apply { seed = "kodee".hashCode().toLong() }

    private var sameSideCounter = 0
    private var lastSide: MailboxPosition.HorizontalPosition? = null

    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {

        val generateTower = rng.randf() <= 0.5 // 50% of chances

        if (generateTower) {
            val ver = if (rng.randf() <= 0.2)
                MailboxPosition.VerticalPosition.TOP else MailboxPosition.VerticalPosition.BOTTOM

            val hoz = when {
                lastSide == null -> if (rng.randf() <= 0.5)
                    MailboxPosition.HorizontalPosition.LEFT else MailboxPosition.HorizontalPosition.RIGHT
                sameSideCounter >= 3 -> if (rng.randf() <= 0.75)
                    MailboxPosition.HorizontalPosition.LEFT else MailboxPosition.HorizontalPosition.RIGHT
                else -> if (rng.randf() <= 0.25)
                    if (lastSide == MailboxPosition.HorizontalPosition.LEFT) MailboxPosition.HorizontalPosition.RIGHT else MailboxPosition.HorizontalPosition.LEFT
                else lastSide
            }

            sameSideCounter = when (hoz) {
                lastSide -> sameSideCounter + 1
                else -> 1
            }
            lastSide = hoz

            yieldBox(hoz = hoz!!, ver = ver, distanceFromPrevious = 1.0)
        }
    }

}