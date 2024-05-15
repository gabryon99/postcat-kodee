package me.gabryon.kodeedelivery.managers

import godot.Node3D
import godot.PackedScene
import godot.TextureRect
import godot.core.Quaternion
import godot.core.Vector3
import godot.core.asStringName
import godot.extensions.callDeferred
import godot.extensions.instanceAs
import godot.global.GD
import godot.util.PI
import me.gabryon.kodeedelivery.actors.Kodee
import me.gabryon.kodeedelivery.actors.MailBox
import me.gabryon.kodeedelivery.levels.LevelLogic
import me.gabryon.kodeedelivery.levels.MailboxPosition
import me.gabryon.kodeedelivery.utility.debug
import me.gabryon.kodeedelivery.utility.getParentAs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

class MailBoxManager(
    private var currentLevelLogic: LevelLogic,
    private val world: Node3D,
    private val kodee: Kodee,
    private val sideOffset: Double,
    private val bottomMailbox: PackedScene,
    private val topMailbox: PackedScene,
    private val scoreManager: ScoreManager,
    private val warningSigns: Array<TextureRect>
) {

    data class MailBoxWrapper(val mailboxParent: Node3D, val pos: MailboxPosition)

    private var currentMailboxIterator: Iterator<MailboxPosition>? = null
    private var currentMailbox: MailboxPosition? = null
    private var currentMailboxDistanceLeft: Double = 0.0

    private val generatedBoxes = ArrayDeque<MailBoxWrapper>()

    private var mailboxId: Int = 0

    /**
     * Create a new type of [MailBox] to add to the current scene. When a new mailbox
     * is created, it is automatically connected to the [ScoreManager].
     */
    private fun createMailBox(
        mailboxScene: PackedScene,
        hozPosition: MailboxPosition.Horizontal,
    ): Node3D {

        val mailboxOrbitPoint = Node3D().apply {
            position = Vector3.ZERO
            name = "Box-orbit-${mailboxId}".asStringName().also { mailboxId++ }
        }

        // debug("New mailbox ${mailboxOrbitPoint.name} - angle: ${GD.radToDeg(angle)}, Kodee Angle: ${GD.radToDeg(kodee.rotationX)}")

        val mailbox = mailboxScene.instanceAs<Node3D>()
        requireNotNull(mailbox) { "Could not instantiate a new MailBox Scene." }

        mailboxOrbitPoint.addChild(mailbox)

        val localYRotation = hozPosition.selectBoxLocalYRotation()
        val positionOffset = hozPosition.selectBoxOffset()

        // Then, connect the signal to the score manager.
        mailbox.apply {
            rotation = Vector3(0, localYRotation, 0)
            position = Vector3(positionOffset, 1.0, 0)
            scale = Vector3.ONE * 0.4
        }

        val mailboxScript = mailbox as? MailBox
        requireNotNull(mailboxScript) { "No MailBox script attached to the MailBox scene: $mailboxScene" }
        mailboxScript.scored.connect(scoreManager, ScoreManager::increaseScore)
        mailboxScript.flightDirection = if (hozPosition == MailboxPosition.Horizontal.LEFT) {
            MailBox.LEFT_FLIGHT_DIRECTION
        } else {
            MailBox.RIGHT_FLIGHT_DIRECTION
        }

        return mailboxOrbitPoint
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

            val kodeeOrbitPointQuat = kodee.getParentAs<Node3D>().quaternion
            val projectedMailBoxQuat = Quaternion(
                Vector3(1, 0, 0),
                PI + (distanceTraveledThisFrame - currentMailboxDistanceLeft)
            )
            val actualMailBoxQuat = kodeeOrbitPointQuat * projectedMailBoxQuat

            val mailboxOrbitPoint = createMailBox(scene, mailbox.hoz)
            mailboxOrbitPoint.quaternion = actualMailBoxQuat

            // Add the mailbox to the world
            world.callDeferred(world::addChild, mailboxOrbitPoint)
            // Add the new mailbox to the queue of generated boxes, to be removed later
            generatedBoxes.add(MailBoxWrapper(mailboxOrbitPoint, mailbox))
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
        // Generate new boxes for the current frame
        spawnBoxes(deltaTime)
        // Generate alerts for incoming mailboxes.
        // alertIncomingMailBoxes() // Currently unused.
    }

    private fun alertIncomingMailBoxes() {

        warningSigns.forEach { it.visible = false }

        // 0: top-left, 1: top-right, 2: bottom-left, 3: bottom-right
        var maximumBoxes = min(max(generatedBoxes.size, 0), 2)
        var prevBox: MailBoxWrapper? = null
        var index = 0

        while (maximumBoxes > 0) {

            val topBox = generatedBoxes[index]
            val dist = kodee.getParentAs<Node3D>().quaternion.angleTo(topBox.mailboxParent.quaternion)
            debug("dist=$dist, deg=${GD.radToDeg(dist)}")

            if (dist <= PI / 2.0) break

            // We need to understand if the prevBox is on the same line of the current
            if (prevBox != null) {
                // The mailboxes are far between each other
                if (topBox.pos.distanceFromPrevious > 0.0) break
            }

            // The `warningSign` texture is inside a 1D array. To find the correct sign
            // to show, we need to find the interesting sign accessing the array as a 2x2 matrix.
            val idx = topBox.pos.hoz.ordinal * 2 + topBox.pos.ver.ordinal
            debug("index: $idx, sign=${warningSigns[idx]}")
            warningSigns[idx].visible = true

            prevBox = topBox
            maximumBoxes--
            index++
        }
    }

    private fun MailboxPosition.Vertical.selectScene(): PackedScene = when (this) {
        MailboxPosition.Vertical.TOP -> topMailbox
        MailboxPosition.Vertical.BOTTOM -> bottomMailbox
    }

    private fun MailboxPosition.Horizontal.selectBoxOffset(): Double = when (this) {
        MailboxPosition.Horizontal.LEFT -> -sideOffset
        MailboxPosition.Horizontal.RIGHT -> sideOffset
    }

    private fun MailboxPosition.Horizontal.selectBoxLocalYRotation(): Double = when (this) {
        MailboxPosition.Horizontal.LEFT -> -PI / 2
        MailboxPosition.Horizontal.RIGHT -> PI / 2
    }

}