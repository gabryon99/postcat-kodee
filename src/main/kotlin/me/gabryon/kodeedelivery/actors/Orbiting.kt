package me.gabryon.kodeedelivery.actors

import godot.Node
import godot.Node3D
import godot.annotation.Export
import godot.annotation.RegisterProperty
import godot.global.GD

interface HasParent {
    fun getParent(): Node?
}

interface Orbiting : HasParent {

    @Export
    @RegisterProperty
    var angularSpeed: Double

    @Export
    @RegisterProperty
    var initialAngularSpeed: Double

    @Export
    @RegisterProperty
    var maximumAngularSpeed: Double

    @Export
    @RegisterProperty
    var deltaSpeed: Double
}

fun Orbiting.orbit(delta: Double) {
    val orbitPoint = getParent() as Node3D
    orbitPoint.rotateX((delta * angularSpeed).toFloat())
}

fun Orbiting.accelerate(delta: Double = deltaSpeed, maxSpeed: Double = maximumAngularSpeed) {
    angularSpeed = GD.clamp(angularSpeed + delta, initialAngularSpeed, maxSpeed)
}

fun Orbiting.decelerate(delta: Double = deltaSpeed) {
    accelerate(-delta)
}
