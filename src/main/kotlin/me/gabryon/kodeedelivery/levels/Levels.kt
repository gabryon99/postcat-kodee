package me.gabryon.kodeedelivery.levels

import me.gabryon.kodeedelivery.utility.increasedBy
import me.gabryon.kodeedelivery.utility.infiniteSequence

data object DebugLevel0 : LevelLogic {

    override val maximumCharacterSpeed: Double = -2.0
    override val pointsToNextLevel: Int = 500

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

    override val maximumCharacterSpeed: Double = -2.0 increasedBy 10.0
    override val pointsToNextLevel: Int = -1

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
 */
data object Level1 : LevelLogic {
    override val maximumCharacterSpeed: Double
        get() = -2.0
    override val pointsToNextLevel: Int
        get() = 2500

    private var sameSideCounter = 0
    private var lastSide: MailboxPosition.HorizontalPosition? = null

    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {

        val generateTower = rng.randf() <= 0.5 // 50% of chances

        if (generateTower) {

            val ver = randomVerticalPosition(0.2)

            val hoz = when {
                lastSide == null -> randomHorizontalPosition(0.5)
                sameSideCounter >= 3 -> randomHorizontalPosition(0.75)
                else -> if (rng.randf() <= 0.25)
                    if (lastSide == MailboxPosition.HorizontalPosition.LEFT)
                        MailboxPosition.HorizontalPosition.RIGHT
                    else
                        MailboxPosition.HorizontalPosition.LEFT
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

/**
 * Level 2:
 * - Each tower can have at most two towers per face (it can also have 0).
 * - The probability that two towers appear is quite low (25%)
 * - The probability that a tall tower appears is low (4/10 will be tall).
 * - If two towers appear on one face, they are definitely at the same height.
 * - Assuming on a face with only one tower that the current tower appeared on the left, then it is LIKELY
 *   (about a 50% chance) that the next one will be generated on the same side. This probability remains
 *   up to 5 towers, from that point the probability of being generated on the same side is 5%.
 * - The speed increases by 5%
 */
data object Level2 : LevelLogic {

    override val maximumCharacterSpeed = Level1.maximumCharacterSpeed increasedBy 5.0

    override val pointsToNextLevel = -1

    private var sameSideCounter = 0
    private var lastSide: MailboxPosition.HorizontalPosition? = null

    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {

        val generateOneTower = rng.randf() <= 0.50 // 50% chances of one tower appearing
        val generateTwoTowers = rng.randf() <= 0.50 // 25% chances of two towers appearing

        // Conditions are mutual-exclusive
        when {
            generateTwoTowers -> generateTwoTowers()
            !generateTwoTowers && generateOneTower -> generateOneTower()
        }
    }

    private suspend fun SequenceScope<MailboxPosition>.generateTwoTowers() {
        val ver = randomVerticalPosition(0.4)
        yieldBox(hoz = MailboxPosition.HorizontalPosition.LEFT, ver = ver, distanceFromPrevious = 1.0)
        yieldBox(hoz = MailboxPosition.HorizontalPosition.RIGHT, ver = ver, distanceFromPrevious = 0.0)
    }

    private suspend fun SequenceScope<MailboxPosition>.generateOneTower() {
        val ver = randomVerticalPosition(0.4)

        val hoz = when {
            lastSide == null -> randomHorizontalPosition(0.5)
            sameSideCounter >= 5 -> randomHorizontalPosition(0.05)
            else -> if (rng.randf() <= 0.50)
                if (lastSide == MailboxPosition.HorizontalPosition.LEFT)
                    MailboxPosition.HorizontalPosition.RIGHT
                else
                    MailboxPosition.HorizontalPosition.LEFT
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

/**
 * Level 3:
 * - Each face can have at most two towers (it can also have 0).
 * - The probability of two towers appearing is VERY low (15%).
 * - The probability that a tall tower appears is a bit higher than normal (60% will be tall).
 * - If two towers appear on one face, it is VERY LIKELY (75%) that they will be at the same height
 *   After this condition occurs for 3 consecutive faces, then the next tower where two towers appear will definitely have different heights
 * - Assuming on a face with only one tower that the current tower appeared on the left, then the next one will DEFINITELY be from the opposite side
 * - The speed increases by 5% from the last past level
 */
data object Level3 : LevelLogic {

    override val maximumCharacterSpeed = Level2.maximumCharacterSpeed increasedBy 5.0

    override val pointsToNextLevel = -1 // Never reachable :)

    private var sameHeightCounter = 0
    private var lastSide: MailboxPosition.HorizontalPosition? = null

    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {

        val generateOneTower = rng.randf() >= 0.15 // 85% chances of one tower appearing
        val generateTwoTowers = rng.randf() < 0.15 // 15% chances of two towers appearing

        when {
            generateTwoTowers -> generateTwoTowers()
            generateOneTower -> generateOneTower()
        }
    }

    private suspend fun SequenceScope<MailboxPosition>.generateTwoTowers() {
        val sameHeight = when {
            sameHeightCounter > 3 -> false
            else -> rng.randf() <= 0.75
        }

        val ver1 = randomVerticalPosition(0.6)
        val ver2 = if (sameHeight) ver1 else randomVerticalPosition(0.6)

        sameHeightCounter = if (sameHeight) sameHeightCounter + 1 else 0

        yieldBox(hoz = MailboxPosition.HorizontalPosition.LEFT, ver = ver1, distanceFromPrevious = 1.0)
        yieldBox(hoz = MailboxPosition.HorizontalPosition.RIGHT, ver = ver2, distanceFromPrevious = 0.0)
    }

    private suspend fun SequenceScope<MailboxPosition>.generateOneTower() {
        val ver = randomVerticalPosition(0.6)
        val hoz = if (lastSide == MailboxPosition.HorizontalPosition.LEFT)
            MailboxPosition.HorizontalPosition.RIGHT
        else
            MailboxPosition.HorizontalPosition.LEFT

        lastSide = hoz

        yieldBox(hoz = hoz, ver = ver, distanceFromPrevious = 1.0)
    }
}