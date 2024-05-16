package me.gabryon.kodeedelivery.actors

import godot.PathFollow3D
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty

@RegisterClass
class PathFollower : PathFollow3D() {

    @Export
    @RegisterProperty
    var speed = 0.1f

    @Export
    @RegisterProperty
    lateinit var kodee: Kodee

    @RegisterFunction
    override fun _process(delta: Double) {
        progressRatio += (speed * delta.toFloat() * kodee.angularSpeed.toFloat())
    }

}