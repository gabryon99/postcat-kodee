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
    orbitPoint.rotateY((angularSpeed * delta).toFloat())
}

fun Orbiting.accelerate(delta: Double = deltaSpeed, maxSpeed: Double = Double.NEGATIVE_INFINITY) {
    // NOTE: speeds are in negative, signs are swapped.
    if (angularSpeed <= maximumAngularSpeed) {
        return
    }
    angularSpeed = GD.clamp(angularSpeed - delta, maxSpeed, initialAngularSpeed)
}

fun Orbiting.decelerate(delta: Double = deltaSpeed) {
    // NOTE: speeds are in negative, signs are swapped.
    if (angularSpeed >= initialAngularSpeed) {
        return
    }
    angularSpeed = GD.clamp(angularSpeed + delta, maximumAngularSpeed, initialAngularSpeed)
}