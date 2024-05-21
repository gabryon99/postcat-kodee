package me.gabryon.kodeedelivery.levels

import me.gabryon.kodeedelivery.utility.infiniteSequence

const val MAXIMUM_CHARACTER_SPEED = 1.5

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

class Level1(
    override val maximumCharacterSpeed: Double = 0.3,
    override val pointsToNextLevel: Int = DEFAULT_POINTS_TO_NEXT_LEVEL
) : LevelLogic {

    companion object {
        const val DEFAULT_POINTS_TO_NEXT_LEVEL = 600 // (4 Mailboxes)
    }

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
        yieldBox(hoz = hoz1, ver = ver, distanceFromPrevious = 0.2)
        yieldBox(hoz = hoz2, ver = ver, distanceFromPrevious = 0.2)
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

        yieldBox(hoz = hoz!!, ver = ver, distanceFromPrevious = 0.25)
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
class Level2(
    override val maximumCharacterSpeed: Double = 0.4,
    override val pointsToNextLevel: Int = DEFAULT_POINTS_TO_NEXT_LEVEL
) : LevelLogic {

    companion object {
        const val DEFAULT_POINTS_TO_NEXT_LEVEL = 2400 // Default 1600 (12 Mailboxes)
    }

    private var sameSideCounter = 0
    private var lastSide: MailboxPosition.Horizontal? = null
    private var lastHeight: MailboxPosition.Vertical? = null
    private var firstMailbox = true
    private var countShort = 0

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
        yieldBox(hoz = hoz, ver = MailboxPosition.Vertical.TOP, distanceFromPrevious = 0.5)
        lastHeight = MailboxPosition.Vertical.TOP
    }

    private suspend fun SequenceScope<MailboxPosition>.generateSequence() {
        val ver1 = randomVerticalPosition(0.5)
        val ver2 = ver1.switchDirection()
        val ver3 = ver2.switchDirection()
        val sameSide = rng.randf() <= 0.2 // When true, the sequence has mailboxes all on the same side

        val hoz1 = randomHorizontalPosition(0.5)
        val hoz2 = if (sameSide) hoz1 else hoz1.switchDirection()
        val hoz3 = if (sameSide) hoz2 else hoz2.switchDirection()
        val dist = if (sameSide) 0.3 else 0.15

        yieldBox(hoz = hoz1, ver = ver1, distanceFromPrevious = 0.3)
        yieldBox(hoz = hoz2, ver = ver2, distanceFromPrevious = dist)
        yieldBox(hoz = hoz3, ver = ver3, distanceFromPrevious = dist)

        countShort = 0
    }

    private suspend fun SequenceScope<MailboxPosition>.generateOneTower() {
        val ver = when (lastHeight) {
            MailboxPosition.Vertical.TOP -> MailboxPosition.Vertical.BOTTOM
            MailboxPosition.Vertical.BOTTOM -> MailboxPosition.Vertical.TOP
            else -> randomVerticalPosition(0.5)
        }
        val hoz = when {
            lastSide == null -> randomHorizontalPosition(0.5)
            sameSideCounter >= 3 -> lastSide?.switchDirection() ?: randomHorizontalPosition(0.5)
            else -> if (rng.randf() >= 0.8) lastSide else randomHorizontalPosition(0.5)
        }

        // Reset the counter if we go a different side from the previous mailbox
        sameSideCounter = if (hoz == lastSide) sameSideCounter + 1 else 1
        lastSide = hoz
        lastHeight = ver

        if (ver == MailboxPosition.Vertical.BOTTOM) {
            countShort++
        }

        yieldBox(hoz = hoz!!, ver = ver, distanceFromPrevious = 0.2)
    }
}

/**
 * Level 3:
 * - The main gimmick of this level is the height of the mailboxes
 * - It is 50% probable that the next mailbox has the same horizontal
 *   side as the previous. AT MOST three can be
 * - It is 2% probable that the next mailbox has the same height as
 *   the previous
 * - After two mailboxes of the same height/side the next will be
 *   on the opposite side/height
 * - 30% probable that a mailbox is short (70% tall)
 * - Sequences are of 3 mailboxes are 50% probable:
 */
class Level3(
    override val maximumCharacterSpeed: Double = 0.5, override val pointsToNextLevel: Int = DEFAULT_POINTS_TO_NEXT_LEVEL
) : LevelLogic {

    companion object {
        const val DEFAULT_POINTS_TO_NEXT_LEVEL = 4200 // Default 2800 (12 Mailboxes)
    }

    private var lastSide: MailboxPosition.Horizontal? = null
    private var lastHeight: MailboxPosition.Vertical? = null
    private var countSides = 0

    private var dist = 0.3 // 0.5 between mailboxes of different sequences
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
        val twoMailboxes = rng.randf() <= 0.4
        numMailboxes = when {
            twoMailboxes -> 2
            else -> 3
        }
    }

    private suspend fun SequenceScope<MailboxPosition>.generateOneTower() {
        val sameSide = countSides < 3 && rng.randf() <= 0.5

        val ver = when (lastHeight) {
            null -> randomVerticalPosition(0.5)
            MailboxPosition.Vertical.TOP -> MailboxPosition.Vertical.BOTTOM
            MailboxPosition.Vertical.BOTTOM -> MailboxPosition.Vertical.TOP
            else -> randomVerticalPosition(0.5)
        }
        val hoz = if (sameSide) {
            lastSide ?: randomHorizontalPosition(0.5)
        } else {
            lastSide?.switchDirection() ?: randomHorizontalPosition(0.5)
        }

        if (sameSide) countSides++

        yieldBox(hoz = hoz, ver = ver, distanceFromPrevious = dist)

        dist = 0.25
        lastSide = hoz
        lastHeight = ver
        numMailboxes--
    }
}

/**
 * Level 4:
 * Only Sequences are in this level
 * Sequences can have at most 5 mailboxes
 *      - 2 mailboxes : 5% probable
 *      - 3 mailboxes : 50% probable
 *      - 4 mailboxes : 25% probable
 *      - 5 mailboxes : 20% probable
 * The probability to have a tall tower is 60%
 * Having mailboxes of the same height is 80% probable
 * A mailbox is 40% probable to be on the same side
 * Only two tall mailbox in a row can be of the same size
 * After five mailboxes on the same side the next will
 * be on the opposite side
 * speed stays the same as the previous level
 */

class Level4(
    override val maximumCharacterSpeed: Double = 0.6,
    override val pointsToNextLevel: Int = DEFAULT_POINTS_TO_NEXT_LEVEL
) : LevelLogic {

    companion object {
        const val DEFAULT_POINTS_TO_NEXT_LEVEL = 6000 // Default 4000 (12 Mailboxes)
    }

    private var lastSide: MailboxPosition.Horizontal? = null
    private var lastHeight: MailboxPosition.Vertical? = null
    private var sameHeight: Boolean = false
    private var numMailboxes: Int = 0  // tells how many mailboxes are left in a sequence
    private var countHeight: Int = 0   // tells how many tall mailboxes are in sequence

    private var countSides = 0

    private var dist = 0.3  // when in a sequence (numMailboxes>0) dist = 0.4
    // otherwise dist = 0.6 (start of a new sequence)

    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {
        when (numMailboxes) {
            0 -> setSequence()
            else -> generateOneTower()
        }
    }

    // Set all the parameters for a sequence (num of mailboxes and if they will have the same height)
    private fun setSequence() {
        dist = 0.2
        lastHeight = null

        // Decides if the new sequence will have mailboxes
        // of the same height
        sameHeight = rng.randf() >= 0.8 // 20%

        val threeMailboxes = rng.randf() <= 0.55  // 50% probable
        val fourMailboxes = rng.randf() <= 0.25   // 30% probable
        val fiveMailboxes = rng.randf() <= 0.2   // 50% probable

        numMailboxes = when {
            threeMailboxes -> 3
            fourMailboxes -> 4
            fiveMailboxes -> 5
            else -> 1
        }
        countSides = 0
    }

    private suspend fun SequenceScope<MailboxPosition>.generateOneTower() {
        val ver = when (lastHeight) {
            MailboxPosition.Vertical.BOTTOM -> MailboxPosition.Vertical.TOP
            MailboxPosition.Vertical.TOP -> MailboxPosition.Vertical.BOTTOM
            else -> randomVerticalPosition(0.5)
        }

        val sameSide = (countSides >= 5) && (rng.randf() <= 0.4)
        val hoz = if (sameSide) {
            lastSide ?: randomHorizontalPosition(0.5)
        } else {
            lastSide?.switchDirection() ?: randomHorizontalPosition(0.5)
        }

        if (sameSide)
            countSides++

        if (ver == lastHeight) {
            countHeight++
            dist = 0.17
        }

        yieldBox(hoz = hoz, ver = ver, distanceFromPrevious = dist)

        lastSide = hoz
        lastHeight = ver
        dist = 0.15
        numMailboxes--
    }
}

/**
 * Level 5:
 * - The main gimmick of this level is the sequences of mailboxes in the same side
 * - They can be of:
 *      - 2 Mailboxes : 15% probable
 *      - 3 Mailboxes : 25% probable
 *      - 4 Mailboxes : 20% probable
 *      - 5 Mailboxes : 60% probable
 * - The first mailbox in a sequence is always tall
 */
class Level5(
    override val maximumCharacterSpeed: Double = 0.7,
    override val pointsToNextLevel: Int = DEFAULT_POINTS_TO_NEXT_LEVEL
) : LevelLogic {

    companion object {
        const val DEFAULT_POINTS_TO_NEXT_LEVEL = 8250 //Default 5200 (15 Mailboxes)
    }

    private var dist: Double? = 0.2
    private var numMailboxes: Int = 0
    private var firstInSequence: Boolean = true
    private var lastSide: MailboxPosition.Horizontal? = null
    private var firstVisit: Boolean = true
    private var secondInSequence: Boolean = false

    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {
        when (numMailboxes) {
            0 -> setSequence()
            else -> generateOneTower()
        }
    }

    // Set all the parameters for a sequence (num of mailboxes and if they will have the same height)
    private fun setSequence() {

        val fourMailboxes = rng.randf() <= 0.50
        val fiveMailboxes = rng.randf() <= 0.40
        val sixMailboxes = rng.randf() <= 0.10

        numMailboxes = when {
            fourMailboxes -> 4
            !fourMailboxes && fiveMailboxes -> 5
            sixMailboxes -> 6
            else -> 4
        }
        firstInSequence = true
        lastSide = lastSide?.switchDirection() ?: randomHorizontalPosition(0.5)

        if (!firstVisit)
            dist = 0.3
        firstVisit = false
    }

    private suspend fun SequenceScope<MailboxPosition>.generateOneTower() {
        val ver = if (firstInSequence) MailboxPosition.Vertical.TOP else MailboxPosition.Vertical.BOTTOM
        val hoz = if (lastSide == null) randomHorizontalPosition(0.5) else lastSide
        if (firstInSequence) {
            secondInSequence = true
        }
        if (secondInSequence) {
            dist = 0.25
            secondInSequence = false
        }
        yieldBox(hoz = hoz!!, ver = ver, distanceFromPrevious = dist!!)
        numMailboxes--
        firstInSequence = false
        lastSide = hoz
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

class Level6(
    override val maximumCharacterSpeed: Double = 0.8,
    override val pointsToNextLevel: Int = DEFAULT_POINTS_TO_NEXT_LEVEL
) : LevelLogic {

    companion object {
        const val DEFAULT_POINTS_TO_NEXT_LEVEL = 10000 // Default 10000 (12 Mailboxes)
    }

    private var dist: Double = 0.25

    override fun mailboxes(): Sequence<MailboxPosition> = infiniteSequence {
        val hoz1 = randomHorizontalPosition(0.5)
        val hoz2 = hoz1.switchDirection()
        val hoz3 = hoz2.switchDirection()
        val hoz4 = hoz3.switchDirection()

        val ver1 = randomVerticalPosition(0.5)
        val ver2 = ver1.switchDirection()
        val ver3 = ver2.switchDirection()
        val ver4 = ver3.switchDirection()

        yieldBox(hoz = hoz1, ver = ver1, distanceFromPrevious = dist)
        yieldBox(hoz = hoz2, ver = ver2, distanceFromPrevious = dist)
        yieldBox(hoz = hoz3, ver = ver3, distanceFromPrevious = dist)
        yieldBox(hoz = hoz4, ver = ver4, distanceFromPrevious = dist)
    }
}