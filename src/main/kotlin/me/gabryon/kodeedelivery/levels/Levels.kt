package me.gabryon.kodeedelivery.levels

import me.gabryon.kodeedelivery.utility.infiniteSequence

const val MAXIMUM_CHARACTER_SPEED = 1.0

/**
 * Level 1:
 * - Only short mailboxes can appear
 * - There is a 30% change to get a sequence of at most 2 mailboxes. The mailboxes in the sequence are
 *   on opposite sides.
 * - Assuming that the current mailbox has appeared to the left, it is VERY LIKELY (about a 75% probability)
 *  that the next to be generated will be on the same side. This is to teach the player to create combos in an
 *  environment that encourages them to do so. This probability holds up to 3 towers, if the third tower is
 *  generated on the same side as the previous two, then the next tower WILL be on the opposite side.
 */

class Level1(override val maximumCharacterSpeed: Double = 0.3) : LevelLogic {
    override val pointsToNextLevel: Int = 1500

    private var sameSideCounter = 0
    private var lastSide: MailboxPosition.Horizontal? = null

    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {
        val generateOneTower = rng.randf() <= 0.7 // 70% chances of one tower appearing
        val generateTwoTowers = rng.randf() <= 0.3 // 30% chances of two tower appearing
        // It may happen that anything is generated!
        when {
            generateTwoTowers -> generateTwoTowers()
            !generateTwoTowers && generateOneTower -> generateOneTower()
        }
    }

    private suspend fun SequenceScope<MailboxPosition>.generateTwoTowers() {
        val ver = MailboxPosition.Vertical.BOTTOM
        val hoz1 = randomHorizontalPosition(0.5)
        val hoz2 = hoz1.switchDirection()
        yieldBox(hoz = hoz1, ver = ver, distanceFromPrevious = 0.7)
        yieldBox(hoz = hoz2, ver = ver, distanceFromPrevious = 0.3)
    }

    private suspend fun SequenceScope<MailboxPosition>.generateOneTower() {
        val ver = MailboxPosition.Vertical.BOTTOM

        val hoz = when {
            lastSide == null -> randomHorizontalPosition(0.5)
            sameSideCounter >= 3 -> lastSide?.switchDirection() ?: randomHorizontalPosition(0.5)
            else -> if (rng.randf() >= 0.8) lastSide else randomHorizontalPosition(0.5)
        }

        // Reset the counter if we go a different side from the previous mailbox
        sameSideCounter = if (hoz == lastSide) sameSideCounter + 1 else 1
        lastSide = hoz

        yieldBox(hoz = hoz!!, ver = ver, distanceFromPrevious = 0.7)
    }
}

/**
 * Level 2:
 * - The first mailbox is ALWAYS tall
 * - A mailbox is 20% probable to be tall
 * - There is a 30% change to get a sequence mailboxes, then:
 *      - is 60% probable to get 2 mailboxes
 *      - is 40% probable to get 3 mailboxes
 * - The mailboxes in a sequence have the same height
 * - In a sequence is 50% possible that the mailboxes are all
 *   on the same side, otherwise they are in a zig-zag pattern
 *  A mailbox is 50% probable to be generated in the same
 *  side as the previous one. This rule holds up to 3 towers, then
 *  the next one will surely be on the opposite side.
 */
class Level2(override val maximumCharacterSpeed: Double = 0.4) : LevelLogic {
    override val pointsToNextLevel = 3500

    private var sameSideCounter = 0
    private var lastSide: MailboxPosition.Horizontal? = null
    private var firstMailbox = true

    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {
        val generateOneTower = rng.randf() <= 0.7 // 70% chance of one mailbox
        val generateSequence = rng.randf() <= 0.3 // 30% chance of a sequence of mailboxes

        // Conditions are mutual-exclusive
        when {
            firstMailbox -> generateTallTower()
            !firstMailbox && generateSequence -> generateSequence()
            generateOneTower -> generateOneTower()
        }
    }

    private suspend fun SequenceScope<MailboxPosition>.generateTallTower() {
        firstMailbox = false
        val hoz = randomHorizontalPosition(0.5)
        yieldBox(hoz = hoz, ver = MailboxPosition.Vertical.TOP, distanceFromPrevious = 0.7)
    }

    private suspend fun SequenceScope<MailboxPosition>.generateSequence() {
        val ver = randomVerticalPosition(0.2)
        val sameSide = rng.randf() <= 0.5 // When true, the sequence has mailboxes all on the same side
        val areThree = rng.randf() <= 0.4 // When true, the sequence is of 3 mailboxes, otherwise 2

        val hoz1 = randomHorizontalPosition(0.5)
        val hoz2 = if (sameSide) hoz1 else hoz1.switchDirection()
        val hoz3 = if (sameSide) hoz2 else hoz2.switchDirection()

        yieldBox(hoz = hoz1, ver = ver, distanceFromPrevious = 0.7)
        yieldBox(hoz = hoz2, ver = ver, distanceFromPrevious = 0.3)

        if (areThree) yieldBox(hoz = hoz3, ver = ver, distanceFromPrevious = 0.3)
    }

    private suspend fun SequenceScope<MailboxPosition>.generateOneTower() {
        val ver = MailboxPosition.Vertical.BOTTOM

        val hoz = when {
            lastSide == null -> randomHorizontalPosition(0.5)
            sameSideCounter >= 3 -> lastSide?.switchDirection() ?: randomHorizontalPosition(0.5)
            else -> if (rng.randf() >= 0.8) lastSide else randomHorizontalPosition(0.5)
        }

        // Reset the counter if we go a different side from the previous mailbox
        sameSideCounter = if (hoz == lastSide) sameSideCounter + 1 else 1
        lastSide = hoz

        yieldBox(hoz = hoz!!, ver = ver, distanceFromPrevious = 0.7)
    }
}

/**
 * Level 3:
 * Only Sequences are in this level
 * Sequences can have at most 5 mailboxes
 *      - 2 mailboxes : 10% probable
 *      - 3 mailboxes : 50% probable
 *      - 4 mailboxes : 10% probable
 *      - 5 mailboxes : 30% probable
 * The probability to have a tall tower is 40%
 * Having mailboxes of the same height is 80% probable
 * A mailbox is 40% probable to be on the same side
 * After five mailboxes on the same side the next will
 * be on the opposite side
 * speed stays the same as the previous level
 */

class Level3(override val maximumCharacterSpeed: Double = 0.5) : LevelLogic {

    override val pointsToNextLevel = 8500

    private var lastSide: MailboxPosition.Horizontal? = null
    private var lastHeight: MailboxPosition.Vertical? = null
    private var sameHeight: Boolean = false
    private var numMailboxes: Int = 0  // tells how many mailboxes are left in a sequence

    private var countSides = 0

    private var dist = 0.6  // when in a sequence (numMailboxes>0) dist = 0.3
    // otherwise dist = 0.6 (start of a new sequence)

    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {
        when (numMailboxes) {
            0 -> setSequence()
            else -> generateOneTower()
        }
    }

    // Set all the parameters for a sequence (num of mailboxes and if they will have the same height)
    private fun setSequence() {
        dist = 0.6
        lastHeight = null

        // Decides if the new sequence will have mailboxes
        // of the same height
        sameHeight = rng.randf() >= 0.8 // 20%

        val twoMailboxes = rng.randf() <= 0.1    // 10% probable
        val threeMailboxes = rng.randf() <= 0.2  // 20% probable
        val fourMailboxes = rng.randf() <= 0.3   // 30% probable
        val fiveMailboxes = rng.randf() <= 0.5   // 50% probable

        numMailboxes = when {
            twoMailboxes -> 2
            !twoMailboxes && threeMailboxes -> 3
            fourMailboxes -> 4
            fiveMailboxes -> 5
            else -> 1
        }
        countSides = 0
    }

    private suspend fun SequenceScope<MailboxPosition>.generateOneTower() {
        val ver = if (sameHeight) {
            lastHeight ?: randomVerticalPosition(0.4)
        } else {
            randomVerticalPosition(0.4) // 40% probable to have a tall mailbox
        }

        val sameSide = (countSides >= 5) && (rng.randf() <= 0.4)
        val hoz = if (sameSide) {
            lastSide ?: randomHorizontalPosition(0.5)
        } else {
            randomHorizontalPosition(0.5)
        }

        if (sameSide)
            countSides++

        yieldBox(hoz = hoz, ver = ver, distanceFromPrevious = dist)

        lastSide = hoz
        lastHeight = ver
        dist = 0.3
        numMailboxes--
    }
}

/**
 * Level 4:
 * - The main gimmick of this level is the height of the mailboxes
 * - It is 20% probable that the next mailbox has the same horizontal
 *   side as the previous
 * - It is 20% probable that the next mailbox ha the same height as
 *   the previous
 * - After three mailboxes of the same height/side the next will be
 *   on the opposite side/height
 * - 30% probable that a mailbox is short (70% tall)
 * - Sequences of at most 3 mailboxes are 50% probable:
 *      - 2 mailboxes: 40% probable
 *      - 3 mailboxes: 60% probable
 */
class Level4(override val maximumCharacterSpeed: Double = 0.6) : LevelLogic {
    override val pointsToNextLevel = 11000

    private var lastSide: MailboxPosition.Horizontal? = null
    private var lastHeight: MailboxPosition.Vertical? = null
    private var countSides = 0
    private var countHeights = 0

    private var dist = 0.5 // 0.5 between mailboxes of different sequences
    // 0.2 between mailboxes of the same sequence

    private var numMailboxes: Int = 0

    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {
        when (numMailboxes) {
            0 -> setSequence()
            else -> generateOneTower()
        }
    }

    // Set all the parameters for a sequence (num of mailboxes and if they will have the same height)
    private fun setSequence() {
        dist = 0.5

        val twoMailboxes = rng.randf() <= 0.4
        val threeMailboxes = rng.randf() <= 0.6

        numMailboxes = when {
            twoMailboxes -> 2
            !twoMailboxes && threeMailboxes -> 3
            else -> 1
        }
    }

    private suspend fun SequenceScope<MailboxPosition>.generateOneTower() {
        val sameHeight = countHeights < 3 && rng.randf() <= 0.2
        val sameSide = countSides < 3 && rng.randf() <= 0.2

        val ver = if (sameHeight) {
            lastHeight ?: randomVerticalPosition(0.7)
        } else {
            randomVerticalPosition(0.7)
        }
        val hoz = if (sameSide) {
            lastSide ?: randomHorizontalPosition(0.5)
        } else {
            randomHorizontalPosition(0.5)
        }

        if (sameHeight) countHeights++
        if (sameSide) countSides++

        yieldBox(hoz = hoz, ver = ver, distanceFromPrevious = dist)

        lastSide = hoz
        lastHeight = ver
        dist = 0.3
        numMailboxes--
    }
}


/**
 * Level 5:
 * - The main gimmick of this level is the distance between mailboxes
 * - Each time a sequence or a single mailbox have to be generated,
 *   a new distance is computed:
 *      0.1 : 10 % probable
 *      0.2 : 20 % probable
 *      0.3 : 20 % probable
 *      0.4 : 30 % probable
 *      0.5 : 10 % probable
 *      0.6 : 10 % probable
 * - Mailboxes in the same sequence have the same distance
 *   between each other
 * - It is 30% probable that the mailbox has the same side
 *   side as the previous.
 * - In a sequence there are at most 5 mailboxes
 * - 80% probable that the mailbox is short
 */
class Level5(override val maximumCharacterSpeed: Double = 0.6) : LevelLogic {
    override val pointsToNextLevel = 13500

    private var dist: Double? = null
    private var lastSide: MailboxPosition.Horizontal? = null
    private var countSides = 0
    private var numMailboxes: Int = 0

    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {
        when (numMailboxes) {
            0 -> setSequence()
            else -> generateOneTower()
        }
    }

    // Set all the parameters for a sequence (num of mailboxes and if they will have the same height)
    private fun setSequence() {
        numMailboxes = (rng.randi() % 5 + 1).toInt()

        val threeDist = rng.randf() <= 0.2
        val fourDist = rng.randf() <= 0.3
        val fiveDist = rng.randf() <= 0.1
        val sixDist = rng.randf() <= 0.1

        dist = if (dist == null) {
            0.6
        } else {
            when {
                threeDist -> 0.3
                !threeDist && fourDist -> 0.4
                fiveDist -> 0.5
                !fiveDist && sixDist -> 0.6
                else -> 0.3
            }
        }
    }

    private suspend fun SequenceScope<MailboxPosition>.generateOneTower() {
        val sameSide = when {
            countSides >= 5 || lastSide == null -> false
            else -> rng.randf() <= 0.3
        }

        val ver = randomVerticalPosition(0.2)
        val hoz = if (sameSide) lastSide else randomHorizontalPosition(0.5)
        yieldBox(hoz = hoz!!, ver = ver, distanceFromPrevious = dist!!)

        lastSide = hoz
        numMailboxes--
    }
}


/**
 * Level 6:
 * - the gimmick of this level is that there is a zig zag pattern costantly
 * - So each mailbox will be on a position different than the last one
 * - the distance is set at 0.2 for all the level
 * - spawn rate of a vertical mailbox is 50% probable
 * - there can be at most 2 tall mailboxes in a row
 * - speed increase by 0.1
 */

class Level6(override val maximumCharacterSpeed: Double = 0.6) : LevelLogic {
    override val pointsToNextLevel = -1 // Last level

    private var dist: Double = 0.6
    private var lastSide: MailboxPosition.Horizontal? = null
    private var lastHeight: MailboxPosition.Vertical? = null
    private var countHeight: Int = 0

    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {

        val hoz = lastSide?.switchDirection() ?: randomHorizontalPosition(0.5)
        val ver = if (countHeight >= 2) MailboxPosition.Vertical.BOTTOM else randomVerticalPosition(0.5)

        if (ver == lastHeight) countHeight++
        if (countHeight >= 2) countHeight = 0

        yieldBox(hoz = hoz, ver = ver, distanceFromPrevious = dist)

        lastSide = hoz
        lastHeight = ver
        dist = 0.4
    }
}