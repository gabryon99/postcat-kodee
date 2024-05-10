package me.gabryon.kodeedelivery.actors

import ch.hippmann.godot.utilities.logging.debug
import godot.*
import godot.annotation.*
import godot.core.Vector3
import godot.core.asStringName
import godot.core.times
import godot.signals.signal
import me.gabryon.kodeedelivery.utility.child

@RegisterClass
class MailBox : Node3D() {

    companion object {
        const val SKIP_AREA_GROUP_NAME = "Box-skip-area"
        const val GROUP_NAME = "Box"
        val RIGHT_FLIGHT_DIRECTION = Vector3(1.0, 1.0, 0.0)
        val LEFT_FLIGHT_DIRECTION = Vector3(-1.0, 1.0, 0.0)
    }

    @RegisterSignal
    val scored by signal<Int>("points")

    private var mailDelivered = false

    @Export
    @RegisterProperty
    var score: Int = 200

    @Export
    @RegisterProperty
    var flightSpeed = 3.0
    @Export
    @RegisterProperty
    var flightDirection = Vector3(1.0, 1.0, 0.0)

    private var dogHit = false

    private val letterboxSound by child<AudioStreamPlayer3D>("Box-top/Box-sound")
    private val flightSound by child<AudioStreamPlayer3D>("Box-top/Box-fly-sound")
    private val collisionArea by child<Area3D>("Box-top/Box-collision")
    private val animationPlayer by child<AnimationPlayer>("Box-top/Box-anim-player")
    private val screeNotifier by child<VisibleOnScreenNotifier3D>("Box-top/Box-screen-notifier")

    @RegisterFunction
    override fun _ready() {
        collisionArea.areaEntered.connect(this, MailBox::onBodyEntered)
        screeNotifier.screenExited.connect(this, MailBox::onScreenExit)
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        if (dogHit) {
            position += (delta * flightSpeed * flightDirection)
            rotateX((flightSpeed * delta).toFloat())
            rotateZ((flightSpeed * delta).toFloat())
        }
    }

    @RegisterFunction
    fun onBodyEntered(body: Node3D) {
        when {
            body.isInGroup(Kodee.GROUP_NAME.asStringName()) && !mailDelivered -> {
                animationPlayer.play("close_box".asStringName())
                letterboxSound.play()
                scored.emit(score)
                mailDelivered = true
            }
            body.isInGroup(Dog.GROUP_NAME.asStringName()) && !dogHit -> {
                dogHit = true
                flightSound.play()
            }
        }
    }

    @RegisterFunction
    fun onScreenExit() {
        debug("bye bye from the mailbox!")
        queueFree()
    }
}