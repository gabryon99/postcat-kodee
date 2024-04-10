package me.gabryon.kodeedelivery.managers

import godot.Node3D
import godot.PackedScene
import godot.core.Vector3
import godot.core.asStringName
import godot.extensions.instanceAs
import godot.util.PI
import me.gabryon.kodeedelivery.actors.Kodee
import me.gabryon.kodeedelivery.actors.MailBox
import me.gabryon.kodeedelivery.levels.LevelLogic
import me.gabryon.kodeedelivery.levels.MailboxPosition
import me.gabryon.kodeedelivery.utility.angularDistance
import me.gabryon.kodeedelivery.utility.loop
import kotlin.math.absoluteValue

class MailboxGenerator(
    private var currentLevelLogic: LevelLogic,
    private val world: Node3D,
    private val kodee: Kodee,
    private val sideOffset: Double,
    private val bottomMailbox: PackedScene,
    private val topMailbox: PackedScene,
    private val scoreManager: ScoreManager,
) {

    data class MailboxWrapper(val mailbox: Node3D, val angularPosition: Double)

    private var currentMailboxIterator: Iterator<MailboxPosition>? = null
    private var currentMailbox: MailboxPosition? = null
    private var currentMailboxDistanceLeft: Double = 0.0

    private val boxesSpawnPoint: Double
        get() = kodee.rotationY + PI

    private val generatedBoxes = ArrayDeque<MailboxWrapper>()

    /**
     * Create a new type of Mailbox to add to the current scene. When a new mailbox
     * is created, it is automatically connected to the [ScoreManager].
     */
    private fun createMailbox(mailboxScene: PackedScene, angle: Double, positionOffset: Double): Node3D {

        val mailbox = mailboxScene.instanceAs<Node3D>()?.apply {
            rotation = Vector3(0, angle, 0)
            position = Vector3(0, positionOffset, 0)
        }
        requireNotNull(mailbox) { "Could not instantiate a new Mailbox Scene." }

        val mailboxScript = mailbox.findChild("Mailbox") as MailBox
        mailboxScript.scored.connect(scoreManager, ScoreManager::increaseScore)

        return mailbox
    }

    /**
     * Remove mailboxes that are not visible anymore in the scene.
     */
    private fun despawnBoxes() {
        val kodeeRotation = kodee.rotationY
        var topBox = generatedBoxes.firstOrNull()

        while (topBox != null && angularDistance(topBox.angularPosition, kodeeRotation) <= -PI / 2.0) {
            generatedBoxes.removeFirst() // Since we are peeking we remove the current box
            topBox.mailbox.callDeferred("queue_free".asStringName())
            topBox = generatedBoxes.firstOrNull()
        }
    }

    private fun spawnBoxes(deltaTime: Double) {

        var distanceTraveledThisFrame = kodee.angularSpeed.absoluteValue * deltaTime
        val mailboxIterator =
            (currentMailboxIterator ?: currentLevelLogic.mailboxes().iterator().also { currentMailboxIterator = it })

        fun getNextMailbox(): MailboxPosition = mailboxIterator.next().also {
            currentMailbox = it
            currentMailboxDistanceLeft = it.distanceFromPrevious
        }

        var mailbox = currentMailbox ?: getNextMailbox()

        loop {

            if (distanceTraveledThisFrame < currentMailboxDistanceLeft) {
                currentMailboxDistanceLeft -= distanceTraveledThisFrame
                return
            }

            val scene = if (mailbox.ver == MailboxPosition.VerticalPosition.TOP) topMailbox else bottomMailbox
            val offset = if (mailbox.hoz == MailboxPosition.HorizontalPosition.LEFT) sideOffset else -sideOffset
            val angularPosition =
                boxesSpawnPoint + (distanceTraveledThisFrame - currentMailboxDistanceLeft)

            val parentMailbox = createMailbox(scene, angularPosition, offset)
            // Add the mailbox to the world
            world.callDeferred("add_child".asStringName(), parentMailbox)
            // Add the new mailbox to the queue of generated boxes, to be removed later
            generatedBoxes.add(MailboxWrapper(parentMailbox, angularPosition))

            mailbox = getNextMailbox()
            distanceTraveledThisFrame -= currentMailboxDistanceLeft
        }
    }

    fun changeLevelLogic(levelLogic: LevelLogic) {
        currentLevelLogic = levelLogic
        currentMailboxIterator = null
        currentMailbox = null
    }

    fun runLevelLogic(deltaTime: Double) {
        // Remove old boxes that are not visible anymore
        despawnBoxes()
        // Generate new boxes for the current frame
        spawnBoxes(deltaTime)
    }
}