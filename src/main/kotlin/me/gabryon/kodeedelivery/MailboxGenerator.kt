package me.gabryon.kodeedelivery

import godot.Node
import godot.Node3D
import godot.PackedScene
import godot.RandomNumberGenerator
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterProperty
import godot.core.Vector3
import godot.core.asStringName
import godot.extensions.instanceAs
import godot.util.PI
import me.gabryon.kodeedelivery.actors.Kodee
import me.gabryon.kodeedelivery.actors.MailBox
import me.gabryon.kodeedelivery.levels.LevelLogic
import me.gabryon.kodeedelivery.levels.MailboxPosition
import me.gabryon.kodeedelivery.managers.ScoreManager
import me.gabryon.kodeedelivery.utility.angularDistance
import me.gabryon.kodeedelivery.utility.debugContext
import kotlin.math.absoluteValue

data class MailboxWrapper(val mailbox: Node3D, val angularPosition: Double)

@RegisterClass
class MailboxGenerator : Node() {

    @Export
    @RegisterProperty
    lateinit var world: Node3D

    @Export
    @RegisterProperty
    lateinit var kodee: Kodee

    private val boxesSpawnPoint: Double
        get() = kodee.rotationY + PI

    @Export
    @RegisterProperty
    var randomSeed: String = "UappaLulla"

    @Export
    @RegisterProperty
    var sideOffset: Double = 0.5

    @Export
    @RegisterProperty
    lateinit var scoreManager: ScoreManager

    //region Packed Scenes
    @Export
    @RegisterProperty
    lateinit var bottomMailbox: PackedScene

    @Export
    @RegisterProperty
    lateinit var upperMailbox: PackedScene
    //endregion

    private val generatedBoxes: ArrayDeque<MailboxWrapper> = ArrayDeque()

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

    private lateinit var currentLevelLogic: LevelLogic
    private var currentMailboxIterator: Iterator<MailboxPosition>? = null
    private var currentMailbox: MailboxPosition? = null
    private var currentMailboxDistanceLeft: Double = 0.0

    fun setLevelLogic(levelLogic: LevelLogic) {
        currentLevelLogic = levelLogic
        currentMailboxIterator = null
        currentMailbox = null
    }

    /**
     * Remove mailboxes that are not visible anymore in the scene.
     */
    private fun despawnBoxes() {
        var topBox = generatedBoxes.firstOrNull()
        val kodeeRotation = kodee.rotationY

        while (topBox != null && angularDistance(topBox.angularPosition, kodeeRotation) <= -PI / 2.0) {
            generatedBoxes.removeFirst() // Since we are peeking we remove the current box

//            debugContext {
//                info<MailboxGenerator>("removing box=$topBox")
//            }

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

        while (true) {

            if (distanceTraveledThisFrame < currentMailboxDistanceLeft) {
                currentMailboxDistanceLeft -= distanceTraveledThisFrame
                return
            }

            val scene = if (mailbox.ver == MailboxPosition.VerticalPosition.TOP) upperMailbox else bottomMailbox
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

    fun runLevelLogic(deltaTime: Double) {
        // Remove old boxes that are not visible anymore
        despawnBoxes()
        // Generate new boxes for the current frame
        spawnBoxes(deltaTime)
    }
}