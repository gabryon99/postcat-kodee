package me.gabryon.kodeedelivery.actors

import godot.Node3D
import godot.Time
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.global.GD

@RegisterClass
class SinusoidalAnimation : Node3D() {

    @Export
    @RegisterProperty
    var speed = 0.002

    @Export
    @RegisterProperty
    var amplitude = 0.05

    private val startPosY = position.y

    @RegisterFunction
    override fun _process(delta: Double) {
        positionMutate {
            y = startPosY + amplitude * GD.sin(speed * Time.getTicksMsec())
        }
        position.y = 10.0
    }

}