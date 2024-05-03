package me.gabryon.kodeedelivery.managers

import ch.hippmann.godot.utilities.logging.debug
import godot.Node3D
import godot.PackedScene
import godot.core.Vector3
import godot.core.asStringName
import godot.extensions.instanceAs
import godot.util.PI
import godot.util.TAU
import me.gabryon.kodeedelivery.actors.Dog
import me.gabryon.kodeedelivery.actors.Kodee
import me.gabryon.kodeedelivery.actors.MailBox
import me.gabryon.kodeedelivery.levels.LevelLogic
import me.gabryon.kodeedelivery.levels.MailboxPosition
import me.gabryon.kodeedelivery.utility.angularDistance
import kotlin.math.absoluteValue

class MailBoxGenerator(
    private var currentLevelLogic: LevelLogic,
    private val world: Node3D,
    private val kodee: Kodee,
    private val sideOffset: Double,
    private val bottomMailbox: PackedScene,
    private val topMailbox: PackedScene,
    private val scoreManager: ScoreManager,
    private val dog: Dog,
) {

    data class MailboxWrapper(val mailboxOrbitPoint: Node3D, val angularPosition: Double)

    private var currentMailboxIterator: Iterator<MailboxPosition>? = null
    private var currentMailbox: MailboxPosition? = null
    private var currentMailboxDistanceLeft: Double = 0.0

    private val generatedBoxes = ArrayDeque<MailboxWrapper>()

    private var mailboxId: Int = 0

    /**
     * Create a new type of [MailBox] to add to the current scene. When a new mailbox
     * is created, it is automatically connected to the [ScoreManager].
     */
    private fun createMailBox(
        mailboxScene: PackedScene,
        angle: Double,
        positionOffset: Double,
        localYRotation: Double
    ): Node3D {

        val mailboxOrbitPoint = Node3D().apply {
            rotation = Vector3(angle, 0, 0)
            position = Vector3.ZERO
            name = "Box-orbit-${mailboxId}".asStringName().also { mailboxId++ }
        }

        // debug("New mailbox ${mailboxOrbitPoint.name} - angle: ${GD.radToDeg(angle)}, Kodee Angle: ${GD.radToDeg(kodee.rotationX)}")

        val mailbox = mailboxScene.instanceAs<Node3D>()
        requireNotNull(mailbox) { "Could not instantiate a new MailBox Scene." }

        mailboxOrbitPoint.addChild(mailbox)

        // Then, connect the signal to the score manager.
        mailbox.apply {
            rotation = Vector3(0, localYRotation, 0)
            position = Vector3(positionOffset, 1.0, 0)
            scale = Vector3.ONE * 0.4
        }

        val mailboxScript = mailbox as? MailBox
        requireNotNull(mailboxScript) { "No MailBox script attached to the MailBox scene: $mailboxScene" }
        mailboxScript.scored.connect(scoreManager, ScoreManager::increaseScore)

        return mailboxOrbitPoint
    }

    /**
     * Remove mailboxes that are not visible anymore in the scene.
     */
    private fun despawnBoxes() {
        val kodeeRotation = kodee.parentRotationX
        var topBox = generatedBoxes.firstOrNull()
        while (topBox != null && angularDistance(topBox.angularPosition, kodeeRotation) >= PI / 2.0) {
            generatedBoxes.removeFirst() // Since we are peeking we remove the current box
            debug("removing: ${topBox.angularPosition}, distance = ${angularDistance(topBox.angularPosition, kodeeRotation)}")
            topBox.mailboxOrbitPoint.callDeferred("queue_free".asStringName()) // TODO: it would be a good idea creating a pool!
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

        while (true) {

            if (distanceTraveledThisFrame < currentMailboxDistanceLeft) {
                currentMailboxDistanceLeft -= distanceTraveledThisFrame
                return
            }

            val scene = mailbox.ver.selectScene()
            val offset = mailbox.hoz.selectBoxOffset()
            val localYRotation = mailbox.hoz.selectBoxLocalYRotation()

            val angularPosition =
                (kodee.parentRotationX + PI + (distanceTraveledThisFrame - currentMailboxDistanceLeft)).mod(TAU)

            // debug("kodee = ${kodee.parentRotationX}, angularPosition = ${angularPosition.deg}")

            val mailboxOrbitPoint = createMailBox(scene, angularPosition, offset, localYRotation)

            // Add the mailbox to the world
            world.callDeferred("add_child".asStringName(), mailboxOrbitPoint)

            // Add the new mailbox to the queue of generated boxes, to be removed later
            generatedBoxes.add(MailboxWrapper(mailboxOrbitPoint, angularPosition))

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

    private fun MailboxPosition.VerticalPosition.selectScene(): PackedScene = when (this) {
        MailboxPosition.VerticalPosition.TOP -> topMailbox
        MailboxPosition.VerticalPosition.BOTTOM -> bottomMailbox
    }

    private fun MailboxPosition.HorizontalPosition.selectBoxOffset(): Double = when (this) {
        MailboxPosition.HorizontalPosition.LEFT -> -sideOffset
        MailboxPosition.HorizontalPosition.RIGHT -> sideOffset
    }

    private fun MailboxPosition.HorizontalPosition.selectBoxLocalYRotation(): Double = when (this) {
        MailboxPosition.HorizontalPosition.LEFT -> -PI / 2
        MailboxPosition.HorizontalPosition.RIGHT -> PI / 2
    }


}