package me.gabryon.kodeedelivery

import godot.CSGCylinder3D
import godot.Node
import godot.Node3D
import godot.PackedScene
import godot.RandomNumberGenerator
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.core.Vector3
import godot.core.asStringName
import godot.extensions.instanceAs
import godot.global.GD
import godot.util.PI
import me.gabryon.kodeedelivery.actors.MailBox
import me.gabryon.kodeedelivery.managers.ScoreManager

@RegisterClass
class MailboxGenerator : Node() {

    @Export
    @RegisterProperty
    var numberOfTowers: Int = 10

    @Export
    @RegisterProperty
    lateinit var world: CSGCylinder3D

    @Export
    @RegisterProperty
    var randomSeed: String = "UappaLulla"

    @Export
    @RegisterProperty
    var sideOffset: Double = 0.5

    @Export
    @RegisterProperty
    lateinit var scoreManager: ScoreManager

    @Export
    @RegisterProperty
    lateinit var bottomMailbox: PackedScene
    @Export
    @RegisterProperty
    lateinit var upperMailbox: PackedScene

    val randGen = RandomNumberGenerator().apply {
        seed = randomSeed.hashCode().toLong()
    }

    /**
     * Create a new type of Mailbox to add to the current scene. When a new mailbox
     * is created, it is automatically connected to the [ScoreManager].
     */
    private fun createMailbox(mailboxScene: PackedScene, angle: Double, off: Double) {
        val mailbox = mailboxScene.instanceAs<Node3D>()?.apply {
            rotation = Vector3(0, GD.radToDeg(angle), 0)
            position = Vector3(0, off, 0)
        }
        requireNotNull(mailbox) { "Could not instantiate a new MailboxScene." }

        val mailboxScript = mailbox.findChild("Mailbox") as MailBox
        requireNotNull(mailbox) { "Could not fetch the Mailbox script." }

        mailboxScript.scored.connect(scoreManager, ScoreManager::increaseScore)

        world.callDeferred("add_child".asStringName(), mailbox)
    }

    /**
     * Generates a random mailbox scene.
     *
     * @return The randomly selected mailbox scene.
     */
    private fun getRandomMailboxScene(): PackedScene {
        val mailboxHeight = randGen.randf()
        return when {
            mailboxHeight >= 0.5 -> bottomMailbox
            else -> upperMailbox
        }
    }

    @RegisterFunction
    override fun _ready() {

        var step = 2 * PI
        var skip = false

        for (i in 0..(numberOfTowers / 2)) {
            if (skip) { skip = false; continue }

            val angle = step * i
            skip = true

            val mailboxPosition = randGen.randf()

            // rnd \in [0, 0.3) => 1 mailbox left
            // rnd \in [0.3, 0.6) => 1 mailbox right
            // rnd \in [0.6, 1) => 2 mailboxes per side

            when {
                mailboxPosition in 0.0 ..< 0.3 -> {
                    createMailbox(getRandomMailboxScene(), angle, sideOffset)
                }
                 mailboxPosition in 0.3 ..< 0.6 -> {
                    createMailbox(getRandomMailboxScene(), angle, -sideOffset)
                }
                else -> {
                    createMailbox(getRandomMailboxScene(), angle, sideOffset)
                    createMailbox(getRandomMailboxScene(), angle, -sideOffset)
                }
            }
        }
    }

}