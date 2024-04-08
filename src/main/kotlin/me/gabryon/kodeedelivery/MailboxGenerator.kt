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
import godot.global.GD
import godot.util.PI
import me.gabryon.kodeedelivery.actors.Kodee
import me.gabryon.kodeedelivery.actors.MailBox
import me.gabryon.kodeedelivery.levels.LevelLogic
import me.gabryon.kodeedelivery.levels.MailboxHorizontalPosition
import me.gabryon.kodeedelivery.levels.MailboxVerticalPosition
import me.gabryon.kodeedelivery.managers.ScoreManager
import me.gabryon.kodeedelivery.utility.debugContext

@RegisterClass
class MailboxGenerator : Node() {

    @Export
    @RegisterProperty
    lateinit var world: Node3D

    @Export
    @RegisterProperty
    lateinit var kodee: Kodee

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

    /**
     * How many batches of mailboxes should be spawned before of Kodee.
     */
    @Export
    @RegisterProperty
    var spawnBatch: Int = 3

    private val randGen = RandomNumberGenerator().apply {
        seed = randomSeed.hashCode().toLong()
    }

    /**
     * Create a new type of Mailbox to add to the current scene. When a new mailbox
     * is created, it is automatically connected to the [ScoreManager].
     */
    private fun createMailbox(mailboxScene: PackedScene, angle: Double, positionOffset: Double) {

        val mailbox = mailboxScene.instanceAs<Node3D>()?.apply {
            rotation = Vector3(0, GD.radToDeg(angle), 0)
            position = Vector3(0, positionOffset, 0)
        }
        requireNotNull(mailbox) { "Could not instantiate a new MailboxScene." }

        val mailboxScript = mailbox.findChild("Mailbox") as MailBox
        requireNotNull(mailbox) { "Could not fetch the Mailbox script." }

        mailboxScript.scored.connect(scoreManager, ScoreManager::increaseScore)

        world.callDeferred("add_child".asStringName(), mailbox)
    }

    fun spawnMailboxes(levelLogic: LevelLogic) {

        if (spawnBatch == 0) return

        var kodeeAngle = kodee.rotation.y
        var deltaAngle = 2*PI  // 45 degrees

        repeat(spawnBatch) {

            when (levelLogic.mailboxesPerFace(randGen)) {
                1 -> {
                    val (hPos, vPos) = levelLogic.generateMailbox(randGen)
                    val scene = if (vPos == MailboxVerticalPosition.UP) {
                        upperMailbox
                    } else {
                        bottomMailbox
                    }
                    val horizontalOffset = if (hPos == MailboxHorizontalPosition.LEFT) {
                        -sideOffset
                    } else {
                        sideOffset
                    }
                    createMailbox(scene, kodeeAngle + deltaAngle, horizontalOffset)
                }
                2 -> {
                    val (_, vPos1) = levelLogic.generateMailbox(randGen)
                    val (_, vPos2) = levelLogic.generateMailbox(randGen)
                    val scene1 = if (vPos1 == MailboxVerticalPosition.UP) {
                        upperMailbox
                    } else {
                        bottomMailbox
                    }
                    val scene2 = if (vPos2 == MailboxVerticalPosition.UP) {
                        upperMailbox
                    } else {
                        bottomMailbox
                    }
                    createMailbox(scene1, kodeeAngle + deltaAngle, -sideOffset)
                    createMailbox(scene2, kodeeAngle + deltaAngle, sideOffset)
                }
                else -> {}
            }

            kodeeAngle += deltaAngle
        }

        spawnBatch = 0
    }

}