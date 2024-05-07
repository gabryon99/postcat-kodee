package me.gabryon.kodeedelivery.actors

import godot.*
import godot.annotation.*
import godot.core.asStringName
import godot.signals.signal
import me.gabryon.kodeedelivery.utility.child

@RegisterClass
class MailBox : Node3D() {

    companion object {
        const val SKIP_AREA_GROUP_NAME = "Box-skip-area"
        const val GROUP_NAME = "Box"
    }

    @RegisterSignal
    val scored by signal<Int>("points")

    private var mailDelivered = false

    @Export
    @RegisterProperty
    var score: Int = 200

    private val letterboxSound by child<AudioStreamPlayer3D>("Box-top/Box-sound")
    private val collisionArea by child<Area3D>("Box-top/Box-collision")
    private val animationPlayer by child<AnimationPlayer>("Box-top/Box-anim-player")

    @RegisterFunction
    override fun _ready() {
        collisionArea.areaEntered.connect(this, MailBox::onBodyEntered)
    }

    @RegisterFunction
    fun onBodyEntered(body: Node3D) {
        if (body.isInGroup(Kodee.GROUP_NAME.asStringName()) && !mailDelivered) {
            animationPlayer.play("close_box".asStringName())
            letterboxSound.play()
            scored.emit(score)
            mailDelivered = true
        }
    }
}