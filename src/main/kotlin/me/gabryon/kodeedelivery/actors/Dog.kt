package me.gabryon.kodeedelivery.actors

import godot.*
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.annotation.RegisterSignal
import godot.core.asStringName
import godot.signals.signal
import me.gabryon.kodeedelivery.utility.child

@RegisterClass
class Dog: Node3D(), Orbiting {
    // -68.8 -180, 180
    companion object {
        const val GROUP_NAME = "Boulder"
    }

    @Export
    @RegisterProperty
    lateinit var camera: Camera3D

    @RegisterSignal
    val onKodeeTouched by signal()

    private val animationPlayer by child<AnimationPlayer>()

    override var angularSpeed: Double = 0.0
    override var initialAngularSpeed: Double = 1.0
    override var maximumAngularSpeed: Double = 1.0
    override var deltaSpeed: Double = 1.0

    @RegisterFunction
    override fun _process(delta: Double) {
        //val up = (globalPosition - (getParent()!! as Node3D).globalPosition)
        //lookAt(camera.globalPosition, up.normalized(), useModelFront = true)
    }

    @RegisterFunction
    override fun _physicsProcess(delta: Double) {
        orbit(delta)
    }

    @RegisterFunction
    fun onAreaEntered(area3D: Area3D) {
        // When the dog catches Kodee, the game is over.
        // Save max user score and change to the next scene
        if (area3D.isInGroup(Kodee.GROUP_NAME.asStringName())) {
           onKodeeTouched.emit()
        }
    }

    @RegisterFunction
    fun barkAndJump() {
        animationPlayer.play("bark_and_jump".asStringName())
    }
}
